package com.dawood.peeng.identity.event;

public record PasswordResetEmailEvent(
        String email,
        String name,
        String token
) {}
