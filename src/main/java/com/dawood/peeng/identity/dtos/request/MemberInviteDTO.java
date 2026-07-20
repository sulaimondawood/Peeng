package com.dawood.peeng.identity.dtos.request;

import com.dawood.peeng.identity.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberInviteDTO(
       @NotBlank(message = "Email is required")
       @Email(message = "Invalid email format")
       String email,
       @NotNull(message = "Role type is required") RoleType role
) {
}
