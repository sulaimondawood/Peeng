package com.dawood.peeng.dashboard.dto;

public record MonitorStatsProjection(
        Long activeMonitors,
        Long totalMonitors,
        Long downMonitors,
        Long openedIncidents,
        Double averageLatency
) {
}
