package com.dawood.peeng.membership.dtos.responses;

import java.util.UUID;

import com.dawood.peeng.identity.enums.RoleType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MembershipSessionDTO {
  private UUID tenantId;

  private String workspaceName;

  private RoleType role;
}
