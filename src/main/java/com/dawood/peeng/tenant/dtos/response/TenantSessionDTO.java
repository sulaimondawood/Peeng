package com.dawood.peeng.tenant.dtos.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TenantSessionDTO {

  private UUID id;

  private String workspaceName;

  private String slug;
}