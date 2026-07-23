package com.dawood.peeng.identity.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dawood.peeng.identity.exceptions.*;
import com.dawood.peeng.notification.enums.NotificationChannel;
import com.dawood.peeng.notification.model.NotificationChannelConfig;
import com.dawood.peeng.notification.respository.NotificationChannelConfigRepository;
import com.sun.security.auth.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.dawood.peeng.tenant.enums.TenantStatus;
import com.dawood.peeng.tenant.exceptions.WorkspaceSuspendedException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import com.dawood.peeng.utils.SlugUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdentityService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MembershipRepository membershipRepository;
  private final TenantRepository tenantRepository;
  private final EmailProducer emailProducer;
  private final EmailVerificationTokenRepository tokenRepository;
  private final JwtService jwtService;
  private final NotificationChannelConfigRepository notificationChannelConfigRepository;

  @Transactional
  public RegisterResponseDTO register(RegisterDTO payload) {

    String normalizedEmail = payload.getEmail().trim().toLowerCase();

    if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
      throw new EmailAlreadyExistsException("Email address already exists");
    }

    User newUser = User.builder()
        .email(normalizedEmail)
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

    NotificationChannelConfig channelConfig = NotificationChannelConfig.builder()
            .tenant(newTenant)
            .channel(NotificationChannel.EMAIL)
            .destination(normalizedEmail)
            .enabled(false)
            .build();

    notificationChannelConfigRepository.save(channelConfig);


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

  @Transactional
  public LoginResponseDTO login(LoginDTO payload) {

    String normalizedEmail = payload.getEmail().trim();

    User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
        .orElseThrow(() -> new InvalidCredentialsException("Username or password is incorrect", HttpStatus.BAD_REQUEST,
            ErrorCode.BAD_REQUEST));

    if (user.getStatus() == Status.SUSPENDED) {
      throw new AccountSuspendedException(
          "Your account was suspended",
          HttpStatus.CONFLICT,
          null);
    }

    if (user.getStatus() == Status.DELETED) {
      throw new InvalidCredentialsException(
          "Username or password is incorrect",
          HttpStatus.BAD_REQUEST,
          ErrorCode.BAD_REQUEST);
    }

    if (user.getStatus().equals(Status.LOCKED) && user.getAccountLockedUntil() != null) {
      if (user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
        throw new AccountLockedException(
            "Your account is temporarily locked",
            HttpStatus.CONFLICT,
            ErrorCode.ACCOUNT_LOCKED);
      }

      user.setStatus(Status.ACTIVE);
      user.setFailedLoginAttempts(0);
      user.setAccountLockedUntil(null);

      userRepository.save(user);

    }

    if (!passwordEncoder.matches(payload.getPassword(), user.getPasswordHash())) {

      int attempts = user.getFailedLoginAttempts() + 1;

      user.setFailedLoginAttempts(attempts);
      user.setLastFailedLoginAt(
          LocalDateTime.now());

      if (attempts >= 5) {
        user.setStatus(Status.LOCKED);
        user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));

        userRepository.save(user);
        throw new AccountLockedException("Your account is temporarily locked", null, null);
      }

      userRepository.save(user);

      throw new InvalidCredentialsException("Username or password is incorrect", HttpStatus.BAD_REQUEST,
          ErrorCode.BAD_REQUEST);
    }

    user.setFailedLoginAttempts(0);
    user.setAccountLockedUntil(null);
    userRepository.save(user);

    if (!user.isEmailVerified()) {
      throw new EmailNotVerifiedException("Email is not verified", HttpStatus.UNAUTHORIZED, ErrorCode.ACCESS_DENIED);
    }

    List<Membership> memberships = membershipRepository
        .findAllByUser_Id(user.getId())
        .stream()
        .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
        .filter(m -> m.getTenant().getStatus() == TenantStatus.ACTIVE)
        .toList();

    Map<String, String> claims = new HashMap<>();
    List<MembershipSessionDTO> membershipDTOs = List.of();

    if (memberships.isEmpty()) {
      log.info("User has no memberships");

      if (user.getLastActiveTenantId() != null) {
        user.setLastActiveTenantId(null);
        userRepository.save(user);
      }

      String accessToken = jwtService.generateToken(claims, normalizedEmail);

      LoginResponseDTO response = new LoginResponseDTO();
      response.setAccessToken(accessToken);
      response.setMemberships(membershipDTOs);
      response.setUser(UserSessionDTO.builder()
              .avatarUrl(user.getAvatarUrl())
              .email(user.getEmail())
              .emailVerified(user.isEmailVerified())
              .name(user.getName())
              .build());

      return response;
    }

    UUID lastActiveTenantId = user.getLastActiveTenantId();
    String role;
    Membership membership;

    if (lastActiveTenantId == null) {
      membership = memberships.get(0);

      role = membership.getRole().name();

      lastActiveTenantId = membership.getTenant().getId();

      user.setLastActiveTenantId(lastActiveTenantId);

      userRepository.save(user);

    } else {

      membership = membershipRepository.findByUser_IdAndTenant_Id(user.getId(), lastActiveTenantId)
          .orElseThrow(() -> new InvalidCredentialsException("User is not a member of the workspace",
              HttpStatus.BAD_REQUEST, null));

      role = membership.getRole().name();
    }

    if (!membership.getStatus().equals(MembershipStatus.ACTIVE)) {
      throw new MembershipException("You no longer have access to this workspace", HttpStatus.BAD_REQUEST,
          ErrorCode.BAD_REQUEST);
    }

    Tenant tenant = membership.getTenant();

    if (tenant.getStatus() != TenantStatus.ACTIVE && tenant.getStatus() != TenantStatus.PAST_DUE
        && tenant.getStatus() != TenantStatus.TRIALING) {

      throw new WorkspaceSuspendedException(
          "Workspace is unavailable",
          HttpStatus.CONFLICT,
          ErrorCode.WORKSPACE_SUSPENDED);
    }

    user.setLastLoginAt(LocalDateTime.now());
    userRepository.save(user);

    claims.put("tenantId", lastActiveTenantId.toString());
    claims.put("role", role);

    String accessToken = jwtService.generateToken(claims, normalizedEmail);

    membershipDTOs = memberships.stream()
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

  private String getCurrentUserEmail() {

    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UnauthorizedException(
              "User is not authenticated",
              HttpStatus.UNAUTHORIZED,
              ErrorCode.UNAUTHORIZED
      );
    }

    return authentication.getName();
  }

  public User getCurrentLoggedInUser(){
    return userRepository.findByEmailIgnoreCase(getCurrentUserEmail())
            .orElseThrow(()->new UserNotFoundException("User not found",HttpStatus.NOT_FOUND,ErrorCode.USER_NOT_FOUND));

  }
}
