package com.dawood.peeng.membership.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.membership.enums.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.membership.models.Membership;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
  Optional<Membership> findByUser_EmailAndTenant_Id(
      String email,
      UUID tenantId);

  Optional<Membership> findByUser_IdAndTenant_Id(
      UUID id,
      UUID tenantId);

  List<Membership> findAllByUser_Id(UUID userId);

  Optional<Membership> findByIdAndTenantIdAndMembershipStatus(UUID memberId, UUID tenantId, MembershipStatus status);

  Optional<Membership> findByTenantIdAndMembershipStatus(UUID tenantId, MembershipStatus status);
}
