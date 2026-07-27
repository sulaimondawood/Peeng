package com.dawood.peeng.tenant.repository;

import com.dawood.peeng.tenant.model.WorkspaceInviteToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WorkspaceInviteTokenRepository extends JpaRepository<WorkspaceInviteToken, UUID> {

    Optional<WorkspaceInviteToken> findByToken(String token);

    Optional<WorkspaceInviteToken> findByUser_IdAndTenant_IdAndAcceptedAtIsNull(
            UUID userId,
            UUID tenantId
    );

//    void deleteByUser_IdAndTenant_Id(UUID userId, UUID tenantId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM WorkspaceInviteToken t
        WHERE t.user.id = :userId
          AND t.tenant.id = :tenantId
        """)
    void deleteByUser_IdAndTenant_Id(
            @Param("userId") UUID userId,
            @Param("tenantId") UUID tenantId
    );
}