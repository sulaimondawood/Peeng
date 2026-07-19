package com.dawood.peeng.identity.dtos.response;

public record InvitePreviewResponseDTO(
        String email,
        boolean isAlreadyRegistered,
        String workspaceName
) {}