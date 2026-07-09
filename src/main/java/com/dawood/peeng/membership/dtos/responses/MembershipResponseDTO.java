package com.dawood.peeng.membership.dtos.responses;

import java.util.UUID;

public record MembershipResponseDTO(
        UUID id,
        String name
        ) {
}
