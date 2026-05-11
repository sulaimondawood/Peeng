package com.dawood.peeng.tenant.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.tenant.model.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

}
