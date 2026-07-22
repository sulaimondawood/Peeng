package com.dawood.peeng.dashboard.repository;

import com.dawood.peeng.dashboard.dto.MonitorStatsProjection;
import com.dawood.peeng.monitor.models.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface DashboardRepository extends JpaRepository<Monitor, UUID> {
    @Query("""
    SELECT new com.dawood.peeng.dashboard.dto.MonitorStatsProjection(
        SUM(CASE WHEN m.status='UP' THEN 1 ELSE 0 END),
        COUNT(m),
        SUM(CASE WHEN m.status='DOWN' THEN 1 ELSE 0 END),
        SUM(CASE WHEN m.incidentOpen THEN 1 ELSE 0 END),
        AVG(m.latestResponseTimeMs)
    ) FROM Monitor m
    WHERE m.tenant.id=:tenantId
""")
    MonitorStatsProjection getMonitorStats(UUID tenantId);
}
