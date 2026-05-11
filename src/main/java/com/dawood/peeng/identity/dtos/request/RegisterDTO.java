package com.dawood.peeng.identity.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterDTO {

  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank(message = "Name is required")
  private String name;

  @NotBlank(message = "Workspace name is required")
  private String workspaceName;
}
