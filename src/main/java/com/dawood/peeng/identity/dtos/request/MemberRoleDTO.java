package com.dawood.peeng.identity.dtos.request;

import com.dawood.peeng.identity.enums.RoleType;
import jakarta.validation.constraints.NotNull;

public record MemberRoleDTO(
       @NotNull(message = "Role type is required") RoleType role
) {
}
