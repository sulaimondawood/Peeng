package com.dawood.peeng.identity.dtos.response;

import java.util.List;

import com.dawood.peeng.membership.dtos.responses.MembershipSessionDTO;
import com.dawood.peeng.tenant.dtos.response.TenantSessionDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponseDTO {

  private String accessToken;

  private String refreshToken;

  private UserSessionDTO user;

  private TenantSessionDTO currentTenant;

  private List<MembershipSessionDTO> memberships;

}
