
package com.dawood.peeng.identity.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponseDTO {

  private String email;

  private boolean requiresEmailVerification;
}