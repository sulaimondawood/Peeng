package com.dawood.peeng.monitor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.monitor.dtos.responses.MonitorStatsProjection;
import com.dawood.peeng.monitor.dtos.responses.ResponseTimePointProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.monitor.models.MonitorCheck;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonitorCheckRepository extends JpaRepository<MonitorCheck, UUID> {
    @Query("""
                SELECT new com.dawood.peeng.monitor.dtos.responses.MonitorStatsProjection(
                    0.0, /* Temporary uptime placeholder */
                    COALESCE(AVG(mc.responseTimeMs), 0.0),
                    COALESCE(MAX(mc.responseTimeMs), 0.0),
                    COALESCE(MIN(mc.responseTimeMs), 0.0),
                    CAST(COUNT(mc) AS int),
                    CAST(SUM(CASE WHEN mc.successful = true THEN 1 ELSE 0 END) AS int),
                    CAST(SUM(CASE WHEN mc.successful = false THEN 1 ELSE 0 END) AS int),
                    0 /* Incident count placeholder */
                )
                FROM MonitorCheck mc
                WHERE mc.monitor.tenant.id = :tenantId
                AND mc.monitor.id = :monitorId
            """)
    Optional<MonitorStatsProjection> getStatistics(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

    @Query(value = """
                SELECT 
                   DATE_TRUNC('hour', checked_at) AS timestamp,
                   AVG(response_time_ms) AS responseTimeMs,
                   MIN(response_time_ms) AS minResponseTime,
                   MAX(response_time_ms) AS maxResponseTime,
                   SUM(CASE WHEN successful THEN 1 ELSE 0 END) AS successfulCount  
                FROM monitor_checks
                WHERE tenant_id = :tenantId
                AND monitor_id = :monitorId
                AND checked_at BETWEEN :from AND :to
                GROUP BY timestamp
                ORDER BY timestamp ASC
            """, nativeQuery = true)
    List<ResponseTimePointProjection> getHourlyResponseTimes(
            @Param("tenantId") UUID tenantId,
            @Param("monitorId") UUID monitorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

}
