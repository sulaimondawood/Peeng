package com.dawood.peeng.identity.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8) String newPassword,
        @NotBlank String confirmPassword
) {}