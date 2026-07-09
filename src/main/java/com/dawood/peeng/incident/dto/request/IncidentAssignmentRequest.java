package com.dawood.peeng.incident.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IncidentAssignmentRequest(
        @NotNull(message = "Team member ID is required")
        UUID memberId
) {
}
