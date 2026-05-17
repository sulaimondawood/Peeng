package com.dawood.peeng.identity.mapper;

import com.dawood.peeng.identity.dtos.response.IdentityDTO;
import com.dawood.peeng.identity.models.User;

public class IdentityMapper {

  public static IdentityDTO toDTO(User user) {

    return IdentityDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .emailVerified(user.isEmailVerified())
        .name(user.getName())
        .avatarUrl(user.getAvatarUrl())
        .lastLoginAt(user.getLastLoginAt())
        .deletedAt(user.getDeletedAt())
        .failedLoginAttempts(user.getFailedLoginAttempts())
        .build();

  }

}
