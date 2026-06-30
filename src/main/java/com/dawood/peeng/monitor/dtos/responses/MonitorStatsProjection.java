package com.dawood.peeng.monitor.dtos.responses;

public record MonitorStatsProjection(
    double uptimePercentage,

    double averageResponseTime,

    double maxResponseTime,

    double minResponseTime,

    int totalChecks,

    int successfulChecks,

    int failedChecks,

    int incidentCount

) {
}
