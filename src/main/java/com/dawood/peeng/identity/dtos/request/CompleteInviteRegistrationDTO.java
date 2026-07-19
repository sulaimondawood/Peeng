package com.dawood.peeng.identity.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record CompleteInviteRegistrationDTO(
       @NotBlank(message = "Token is missing") String token,
       @NotBlank(message = "Name is required") String name,
       @NotBlank(message = "Password is required") String password
) {
}
