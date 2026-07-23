package com.dawood.peeng.identity.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dawood.peeng.common.exceptions.PeengException;
import com.dawood.peeng.identity.dtos.response.VerifyEmailResponse;
import com.dawood.peeng.identity.exceptions.*;
import com.dawood.peeng.notification.enums.NotificationChannel;
import com.dawood.peeng.notification.model.NotificationChannelConfig;
import com.dawood.peeng.notification.respository.NotificationChannelConfigRepository;
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
import com.dawood.peeng.membership.mapper.MembershipMapper;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.messaging.events.SendVerificationEmailEvent;
import com.dawood.peeng.messaging.producers.EmailProducer;
import com.dawood.peeng.security.JwtService;
import com.dawood.peeng.tenant.enums.TenantStatus;
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
        String normalizedEmail = payload.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Username or password is incorrect",
                        HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST));

        if (user.getStatus() == Status.SUSPENDED) {
            throw new AccountSuspendedException("Your account was suspended", HttpStatus.CONFLICT, null);
        }
        if (user.getStatus() == Status.DELETED) {
            throw new InvalidCredentialsException("Username or password is incorrect",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        if (user.getStatus() == Status.LOCKED && user.getAccountLockedUntil() != null) {
            if (user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                throw new AccountLockedException("Your account is temporarily locked",
                        HttpStatus.CONFLICT, ErrorCode.ACCOUNT_LOCKED);
            }
            user.setStatus(Status.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
        }

        if (!passwordEncoder.matches(payload.getPassword(), user.getPasswordHash())) {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            user.setLastFailedLoginAt(LocalDateTime.now());

            if (attempts >= 5) {
                user.setStatus(Status.LOCKED);
                user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(30));
            }
            userRepository.saveAndFlush(user);
            throw new InvalidCredentialsException("Username or password is incorrect",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setLastLoginAt(LocalDateTime.now());

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Email is not verified",
                    HttpStatus.UNAUTHORIZED, ErrorCode.ACCESS_DENIED);
        }

        List<Membership> activeMemberships = membershipRepository.findAllByUser_Id(user.getId()).stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .filter(m -> isTenantAccessible(m.getTenant().getStatus()))
                .toList();

        Map<String, String> claims = new HashMap<>();
        List<MembershipSessionDTO> membershipDTOs = activeMemberships.stream()
                .map(MembershipMapper::toSessionDTO)
                .toList();

        UUID lastActiveTenantId = user.getLastActiveTenantId();
        Membership selectedMembership = null;
        String role = null;

        if (!activeMemberships.isEmpty()) {
            final UUID currentLastActiveId = lastActiveTenantId;

            if (currentLastActiveId != null) {
                selectedMembership = activeMemberships.stream()
                        .filter(m -> m.getTenant().getId().equals(currentLastActiveId))
                        .findFirst()
                        .orElse(null);
            }

            if (selectedMembership == null) {
                selectedMembership = activeMemberships.get(0);
                lastActiveTenantId = selectedMembership.getTenant().getId();
                user.setLastActiveTenantId(lastActiveTenantId);
            }

            role = selectedMembership.getRole().name();
            claims.put("tenantId", lastActiveTenantId.toString());
            claims.put("role", role);
        } else {
            log.info("User {} logged in with no active workspaces", normalizedEmail);
            if (user.getLastActiveTenantId() != null) {
                user.setLastActiveTenantId(null);
            }
        }

        userRepository.save(user);

        String accessToken = jwtService.generateToken(claims, normalizedEmail);

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .memberships(membershipDTOs)
                .user(UserSessionDTO.builder()
                        .avatarUrl(user.getAvatarUrl())
                        .email(user.getEmail())
                        .emailVerified(user.isEmailVerified())
                        .name(user.getName())
                        .build())
                .message(activeMemberships.isEmpty()
                        ? "Login successful. You currently have no active workspaces."
                        : null)
                .build();
    }

    public void updateName(String newName) {
        User user = getCurrentLoggedInUser();
        user.setName(newName.trim());
        userRepository.save(user);
    }

    public void updatePassword(String currentPassword, String newPassword, String confirmPassword) {

        if (newPassword == null || newPassword.length() < 8) {
            throw new PeengException("New password must be at least 8 characters",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new PeengException("New password and Confirm password do not match",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        User user = getCurrentLoggedInUser();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect",
                    HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new PeengException("Invalid or expired verification token", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        User user = verificationToken.getUser();

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            user.setToken(null);
            userRepository.save(user);

            EmailVerificationToken newToken = createAndSendNewVerificationToken(user);

            return VerifyEmailResponse.builder()
                    .success(false)
                    .message("This verification link has expired. A new link has been sent to your email.")
                    .email(user.getEmail())
                    .newTokenSent(true)
                    .build();
        }


        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            user.setStatus(Status.ACTIVE);

            tokenRepository.delete(verificationToken);
            user.setToken(null);

            userRepository.save(user);
        }

        return VerifyEmailResponse.builder()
                .success(true)
                .message("Your email has been verified successfully!")
                .email(user.getEmail())
                .build();

    }

    @Transactional
    public void resendVerificationEmail() {
        User user = getCurrentLoggedInUser();

        if (user.isEmailVerified()) {
            throw new PeengException("Email is already verified", HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        tokenRepository.deleteByUser(user);

        EmailVerificationToken newToken = EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(newToken);

        SendVerificationEmailEvent event = SendVerificationEmailEvent.builder()
                .email(user.getEmail())
                .name(user.getName())
                .token(newToken.getToken())
                .build();

        emailProducer.sendVerificationEmail(event);
    }


    private boolean isTenantAccessible(TenantStatus status) {
        return status == TenantStatus.ACTIVE ||
                status == TenantStatus.TRIALING ||
                status == TenantStatus.PAST_DUE;
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

    public User getCurrentLoggedInUser() {
        return userRepository.findByEmailIgnoreCase(getCurrentUserEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

    }

    private EmailVerificationToken createAndSendNewVerificationToken(User user) {

        EmailVerificationToken newToken = EmailVerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(newToken);

        SendVerificationEmailEvent event = SendVerificationEmailEvent.builder()
                .email(user.getEmail())
                .name(user.getName())
                .token(newToken.getToken())
                .build();

        emailProducer.sendVerificationEmail(event);

        return newToken;
    }
}
