package com.dawood.peeng.tenant.repository;

import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.identity.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.tenant.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByIdAndOwner(UUID tenantId, User user);

}
