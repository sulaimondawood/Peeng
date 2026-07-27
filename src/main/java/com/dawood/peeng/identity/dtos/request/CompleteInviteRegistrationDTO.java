package com.dawood.peeng.identity.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record CompleteInviteRegistrationDTO(
       @NotBlank(message = "Token is missing") String token,
       String name,
       String password
) {
}
