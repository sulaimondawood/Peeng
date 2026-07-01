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
    public MonitorStatsProjection{
        if(totalChecks > 0){
            double calculated = ((double) successfulChecks / totalChecks) * 100.0;
            uptimePercentage = Math.round(calculated * 100.0) / 100.0;
        }else{
            uptimePercentage = 100.0;
        }
    }

}
