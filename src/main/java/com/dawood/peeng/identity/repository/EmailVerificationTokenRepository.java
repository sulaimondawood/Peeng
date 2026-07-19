package com.dawood.peeng.identity.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.identity.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.identity.models.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByUserId(UUID uuid);

    void deleteByUser(User user);

    Optional<EmailVerificationToken> findByUserAndExpiresAtAfter(User user, LocalDateTime expiresAtAfter);
}
