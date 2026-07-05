package com.dawood.peeng.incident.dto.response;

public record CheckResult(
        int statusCode,
        long responseTimeMs,
        boolean isTimeout,     
        String errorMessage
) {

}
