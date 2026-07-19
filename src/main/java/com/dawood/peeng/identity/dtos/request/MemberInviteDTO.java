package com.dawood.peeng.identity.dtos.request;

import com.dawood.peeng.identity.enums.RoleType;
import jakarta.validation.constraints.NotNull;

public record MemberInviteDTO(
       @NotNull String email,
       @NotNull RoleType role
) {
}
