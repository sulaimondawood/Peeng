package com.dawood.peeng.identity.service;

import java.time.LocalDateTime;
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
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.messaging.events.SendVerificationEmailEvent;
import com.dawood.peeng.messaging.producers.EmailProducer;
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
      throw new InvalidCredentialsException("Username or password is incorrect", HttpStatus.BAD_REQUEST,
          ErrorCode.BAD_REQUEST);
    }

    if (!user.isEmailVerified()) {
      throw new EmailNotVerifiedException("Email is not verified", HttpStatus.CONFLICT, null);
    }

    if (user.getStatus() == Status.SUSPENDED) {
      throw new AccountSuspendedException(
          "Your account was suspended",
          HttpStatus.CONFLICT,
          null);
    }

    return null;

  }
}
