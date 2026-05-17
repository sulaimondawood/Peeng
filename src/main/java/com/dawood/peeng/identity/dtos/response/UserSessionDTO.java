package com.dawood.peeng.identity.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Builder
@Getter
public class UserSessionDTO {

  private String email;

  private String name;

  private String avatarUrl;

  private boolean emailVerified;
}
