package com.dawood.peeng.dashboard.dto;

public record DashboardOverviewDTO(
        long activeMonitorsCount,
        long totalMonitorsCount,
        long downMonitorsCount,
        long openIncidentsCount,
        double avgLatencyMs
) {}