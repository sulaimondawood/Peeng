package com.dawood.peeng.identity.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.identity.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.identity.models.EmailVerificationToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, UUID> {

    Optional<EmailVerificationToken> findByUserId(UUID uuid);

    void deleteByUser(User user);

    Optional<EmailVerificationToken> findByUserAndExpiresAtAfter(User user, LocalDateTime expiresAtAfter);

    Optional<EmailVerificationToken> findByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EmailVerificationToken t WHERE t.user.id = :userId")
    void deleteByUserId(@Param("userId") UUID userId);
}
