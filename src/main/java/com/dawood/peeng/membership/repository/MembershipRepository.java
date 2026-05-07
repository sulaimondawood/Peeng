package com.dawood.peeng.membership.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.membership.models.Membership;

public interface MembershipRepository extends JpaRepository<Membership, UUID> {
  Optional<Membership> findByUser_EmailAndTenant_Id(
      String email,
      UUID tenantId);
}
