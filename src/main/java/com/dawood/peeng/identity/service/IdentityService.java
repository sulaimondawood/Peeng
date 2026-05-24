package com.dawood.peeng.identity.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.identity.dtos.request.LoginDTO;
import com.dawood.peeng.identity.dtos.request.RegisterDTO;
import com.dawood.peeng.identity.dtos.response.LoginResponseDTO;
import com.dawood.peeng.identity.dtos.response.RegisterResponseDTO;
import com.dawood.peeng.identity.dtos.response.UserSessionDTO;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.enums.Status;
import com.dawood.peeng.identity.exceptions.AccountSuspendedException;
import com.dawood.peeng.identity.exceptions.EmailAlreadyExistsException;
import com.dawood.peeng.identity.exceptions.EmailNotVerifiedException;
import com.dawood.peeng.identity.exceptions.InvalidCredentialsException;
import com.dawood.peeng.identity.models.EmailVerificationToken;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.EmailVerificationTokenRepository;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.membership.dtos.responses.MembershipSessionDTO;
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.exceptions.MembershipException;
import com.dawood.peeng.membership.mapper.MembershipMapper;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.messaging.events.SendVerificationEmailEvent;
import com.dawood.peeng.messaging.producers.EmailProducer;
import com.dawood.peeng.security.JwtService;
import com.dawood.peeng.tenant.dtos.response.TenantSessionDTO;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import com.dawood.peeng.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IdentityService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MembershipRepository membershipRepository;
  private final TenantRepository tenantRepository;
  private final EmailProducer emailProducer;
  private final EmailVerificationTokenRepository tokenRepository;
  private final JwtService jwtService;

  @Transactional
  public RegisterResponseDTO register(RegisterDTO payload) {

    String normailizedEmail = payload.getEmail().trim().toLowerCase();

    if (userRepository.existsByEmailIgnoreCase(normailizedEmail)) {
      throw new EmailAlreadyExistsException("Email address already exists");
    }

    User newUser = User.builder()
        .email(normailizedEmail)
        .passwordHash(passwordEncoder.encode(payload.getPassword()))
        .name(payload.getName())
        .build();

    User savedUser = userRepository.save(newUser);

    EmailVerificationToken token = EmailVerificationToken.builder()
        .token(UUID.randomUUID().toString())
        .user(savedUser)
        .expiresAt(LocalDateTime.now().plusHours(24))
        .build();

    tokenRepository.save(token);

    Tenant newTenant = Tenant.builder()
        .workspaceName(payload.getWorkspaceName())
        .slug(SlugUtils.makeUniqueSlug(payload.getWorkspaceName()))
        .owner(savedUser)
        .build();

    tenantRepository.save(newTenant);

    Membership newMembership = Membership.builder()
        .user(savedUser)
        .role(RoleType.OWNER)
        .tenant(newTenant)
        .joinedAt(LocalDateTime.now())
        .status(MembershipStatus.ACTIVE)
        .build();

    membershipRepository.save(newMembership);

    SendVerificationEmailEvent event = SendVerificationEmailEvent.builder()
        .email(savedUser.getEmail())
        .name(savedUser.getName())
        .token(token.getToken())
        .build();

    if (TransactionSynchronizationManager.isSynchronizationActive()) {

      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          emailProducer.sendVerificationEmail(event);
        }
      });
    }

    return RegisterResponseDTO.builder()
        .email(savedUser.getEmail())
        .requiresEmailVerification(true)
        .message(
            "Registration successful. Please verify your email.")
        .build();

  }

  public LoginResponseDTO login(LoginDTO payload) {

    String normalizedEmail = payload.getEmail().trim();

    User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
        .orElseThrow(() -> new InvalidCredentialsException("Username or password is incorrect", HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST));

    if (!passwordEncoder.matches(payload.getPassword(), user.getPasswordHash())) {

      user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);

      userRepository.save(user);

      throw new InvalidCredentialsException("Username or password is incorrect", HttpStatus.BAD_REQUEST,
          ErrorCode.BAD_REQUEST);
    }

    user.setFailedLoginAttempts(0);
    userRepository.save(user);

    if (!user.isEmailVerified()) {
      throw new EmailNotVerifiedException("Email is not verified", HttpStatus.CONFLICT, null);
    }

    if (user.getStatus() == Status.SUSPENDED) {
      throw new AccountSuspendedException(
          "Your account was suspended",
          HttpStatus.CONFLICT,
          null);
    }

    List<Membership> memberships = membershipRepository.findAllByUser_Id(user.getId());

    if (memberships.isEmpty()) {
      throw new MembershipException("User has no memberships", HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
    }

    UUID lastActiveTenantId = user.getLastActiveTenantId();
    String role;

    if (lastActiveTenantId == null) {
      Membership firstMembership = memberships.get(0);

      role = firstMembership.getRole().name();

      lastActiveTenantId = firstMembership.getTenant().getId();

      user.setLastActiveTenantId(lastActiveTenantId);

      userRepository.save(user);

    } else {

      Membership membership = membershipRepository.findByUser_IdAndTenant_Id(user.getId(), lastActiveTenantId)
          .orElseThrow(() -> new InvalidCredentialsException("User is not a member of the workspace",
              HttpStatus.BAD_REQUEST, null));

      role = membership.getRole().name();
    }

    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);

    Map<String, String> claims = new HashMap<>();
    claims.put("tenantID", lastActiveTenantId.toString());
    claims.put(role, role);

    String accessToken = jwtService.generateToken(claims, normalizedEmail);

    List<MembershipSessionDTO> membershipDTOs = memberships.stream()
        .map(MembershipMapper::toSessionDTO).toList();

    LoginResponseDTO response = new LoginResponseDTO();
    response.setAccessToken(accessToken);
    response.setMemberships(membershipDTOs);
    response.setUser(UserSessionDTO.builder()
        .avatarUrl(user.getAvatarUrl())
        .email(user.getEmail())
        .emailVerified(user.isEmailVerified())
        .name(user.getName()).build());

    return response;

  }
}
