package com.dawood.peeng.tenant.dtos.response;

import java.util.UUID;

import com.dawood.peeng.identity.enums.RoleType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TenantSessionDTO {

  private UUID id;

  private UUID tenantId;

  private String workspaceName;

  private String slug;

  private RoleType role;
}