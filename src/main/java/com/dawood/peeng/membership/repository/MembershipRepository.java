package com.dawood.peeng.membership.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.identity.dtos.response.TeamOverview;
import com.dawood.peeng.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.membership.models.Membership;
import org.springframework.data.jpa.repository.Query;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
  Optional<Membership> findByUser_EmailAndTenant_Id(
      String email,
      UUID tenantId);

  Optional<Membership> findByIdAndTenantId(
          UUID id,
          UUID tenantId);


  Optional<Membership> findByUser_IdAndTenant_Id(
      UUID id,
      UUID tenantId);

  List<Membership> findAllByUser_Id(UUID userId);

  Optional<Membership> findByIdAndTenantIdAndStatus(UUID memberId, UUID tenantId, MembershipStatus status);

  Optional<Membership> findByTenantIdAndStatus(UUID tenantId, MembershipStatus status);

  @Query("""
    SELECT new com.dawood.peeng.identity.dtos.response.TeamOverview(
        CAST(SUM(CASE WHEN m.status IN ('ACTIVE','INVITED','SUSPENDED') THEN 1 ELSE 0 END) AS int),
        CAST(SUM(CASE WHEN m.status='ACTIVE' THEN 1 ELSE 0 END) AS int),
        CAST(SUM(CASE WHEN m.status='INVITED' THEN 1 ELSE 0 END) AS int),
        CAST(SUM(CASE WHEN m.status='SUSPENDED' THEN 1 ELSE 0 END) AS int)
    ) FROM Membership m
    WHERE m.tenant.id = :tenantId
""")
  TeamOverview getTeamOverview(UUID tenantId);

  List<Membership> findAllByTenantIdAndStatusIn(UUID tenantId, Collection<MembershipStatus> statuses);

}
