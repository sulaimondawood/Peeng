package com.dawood.peeng.identity.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentityDTO {

  private UUID id;

  private String email;

  private boolean emailVerified = false;

  private String name;

  private String avatarUrl;

  private LocalDateTime lastLoginAt;

  private LocalDateTime deletedAt;

  private int failedLoginAttempts;

}
