package com.dawood.peeng.monitor.repository;

import java.util.Optional;
import java.util.UUID;

import com.dawood.peeng.monitor.dtos.responses.MonitorStatsProjection;
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

    @Query("""
                SELECT COUNT(mc) FROM MonitorCheck mc
                WHERE mc.monitor.tenant.id=:tenantId
                AND mc.monitor.id=:monitorId
            """)
    long totalCheck(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

    @Query("""
             SELECT COUNT(mc) FROM MonitorCheck mc
             WHERE mc.monitor.tenant.id=:tenantId
             AND mc.successful=true
             AND mc.monitor.id=:monitorId
            """)
    long successfulChecks(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

    @Query("""
             SELECT COUNT(mc) FROM MonitorCheck mc
             WHERE mc.monitor.tenant.id=:tenantId
             AND mc.successful=false
             AND mc.monitor.id=:monitorId
            """)
    long failedChecks(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

    @Query("""
                SELECT COALESCE(AVG(mc.responseTimeMs),0) FROM MonitorCheck mc
                WHERE mc.monitor.tenant.id=:tenantId
                AND mc.monitor.id=:monitorId
            """)
    double averageResponseTime(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

    @Query("""
                SELECT COALESCE(MIN(mc.responseTimeMs),0) FROM MonitorCheck mc
                WHERE mc.monitor.tenant.id=:tenantId
                AND mc.monitor.id=:monitorId
            """)
    double minResponseTime(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

    @Query("""
                SELECT COALESCE(MAX(mc.responseTimeMs),0) FROM MonitorCheck mc
                WHERE mc.monitor.tenant.id=:tenantId
                AND mc.monitor.id=:monitorId
            """)
    double maxResponseTime(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

}
