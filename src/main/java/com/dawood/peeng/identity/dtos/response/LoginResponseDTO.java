package com.dawood.peeng.identity.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

  private String accessToken;

  private Long expiresIn;

  private UserSessionDTO user;

  private TenantSessionDTO currentTenant;

  private List<MembershipSessionDTO> memberships;

}
