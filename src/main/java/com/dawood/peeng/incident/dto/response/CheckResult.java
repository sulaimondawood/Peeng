package com.dawood.peeng.incident.dto.response;

import org.springframework.http.ResponseEntity;

public record CheckResult(
       long startTime,
       ResponseEntity<Void> response,
       String message,
       boolean isTimeout,
       boolean isHighLatency,
       long responseTimeInMs
) {

}
