package com.dawood.peeng.incident.dto.response;

public record IncidentDiagnosticTraceDTO(
        String message,
        int statusCode,
        long responseTimeMs,
        boolean successful


) {
}
