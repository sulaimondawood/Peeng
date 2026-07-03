package com.dawood.peeng.monitor.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.monitor.dtos.responses.MonitorStatsProjection;
import com.dawood.peeng.monitor.dtos.responses.ResponseTimePointProjection;
import com.dawood.peeng.monitor.dtos.responses.UptimeBlockProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.dawood.peeng.monitor.models.MonitorCheck;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonitorCheckRepository extends JpaRepository<MonitorCheck, UUID> {
    @Query("""
                SELECT new com.dawood.peeng.monitor.dtos.responses.MonitorStatsProjection(
                    0.0, /* Temporary uptime placeholder */
                    COALESCE(ROUND(AVG(mc.responseTimeMs), 2), 0.0),
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
                   DATE_BIN('1 minute', checked_at, '1970-01-01 00:00:00') AS timestamp,
                   ROUND(AVG(response_time_ms)::numeric, 2) AS responseTimeMs,
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
    List<ResponseTimePointProjection> findHourlyBucket(
            @Param("tenantId") UUID tenantId,
            @Param("monitorId") UUID monitorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
                SELECT 
                   DATE_BIN('30 minutes', checked_at, '1970-01-01 00:00:00') AS timestamp,
                   ROUND(AVG(response_time_ms)::numeric, 2) AS responseTimeMs,
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
    List<ResponseTimePointProjection> find24hrBucket(
            @Param("tenantId") UUID tenantId,
            @Param("monitorId") UUID monitorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
                SELECT 
                   DATE_BIN('2 hours', checked_at, '1970-01-01 00:00:00') AS timestamp,
                   ROUND(AVG(response_time_ms)::numeric, 2) AS responseTimeMs,
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
    List<ResponseTimePointProjection> find7daysBucket(
            @Param("tenantId") UUID tenantId,
            @Param("monitorId") UUID monitorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query(value = """
                SELECT 
                   DATE_BIN('6 hours', checked_at, '1970-01-01 00:00:00') AS timestamp,
                   ROUND(AVG(response_time_ms)::numeric, 2) AS responseTimeMs,
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
    List<ResponseTimePointProjection> findMonthlyBucket(
            @Param("tenantId") UUID tenantId,
            @Param("monitorId") UUID monitorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);


    @Query(value = """
                SELECT
                    sub.timestamp,
                    sub.responseTimeMs,
                    sub.successfulCount,
                    sub.containedCount,
                    ROUND((sub.successfulCount::numeric / sub.containedCount::numeric) *100,2) AS uptimePercentage
                FROM(
                    SELECT 
                        DATE_BIN(CAST(:timeframe AS interval), checked_at,'1970-01-01 00:00:00') AS timestamp,
                        ROUND(AVG(response_time_ms)::numeric, 2) AS responseTimeMs,
                        SUM(CASE WHEN successful THEN 1 ELSE 0 END) AS successfulCount,
                        COUNT(*) AS containedCount
                    FROM monitor_checks
                    WHERE tenant_id = :tenantId
                    AND monitor_id = :monitorId
                    AND checked_at BETWEEN :from AND :to
                    GROUP BY timestamp
                            ) sub
                ORDER BY timestamp ASC
            """, nativeQuery = true)
    List<UptimeBlockProjection> findUptimeBlocks(
            @Param("tenantId") UUID tenantId,
            @Param("monitorId") UUID monitorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("timeframe") String timeframe
            );


    Page<MonitorCheck> findAllByTenant_IdAndMonitorIdOrderByCheckedAtDesc(
            UUID monitorId,
            UUID tenantId,
            Pageable pageable
    );
}
