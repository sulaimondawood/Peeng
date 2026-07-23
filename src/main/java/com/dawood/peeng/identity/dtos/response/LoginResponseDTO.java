package com.dawood.peeng.identity.dtos.response;

import java.util.List;

import com.dawood.peeng.membership.dtos.responses.MembershipSessionDTO;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {

  private String accessToken;

  private String refreshToken;

  private UserSessionDTO user;

  private String message;

  private List<MembershipSessionDTO> memberships;

}
