package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.models.Incident;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    boolean existsByMonitor_IdAndStatus(UUID monitorId, IncidentStatus status);

    Optional<Incident> findByMonitor_IdAndStatus(UUID monitorId, IncidentStatus status);

    @Query("""
                SELECT COUNT(i) FROM Incident i
                WHERE i.tenant.id=:tenantId
                AND i.monitor.id=:monitorId
            """)
    long incidentCount(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);

    List<Incident> findTop10ByMonitorIdAndTenantIdOrderByStartedAtDesc(UUID monitorId, UUID tenantId);

    Optional<Incident> findByTenantIdAndIdAndStatus(UUID tenantId, UUID incidentId, IncidentStatus status);

    List<Incident> findByTenantIdAndStatus(UUID tenantId, IncidentStatus status);

    @Query("""
                SELECT i FROM Incident i
                WHERE i.tenant.id=:tenantId
                AND (:status IS NULL OR i.status=:status)
                AND (:monitorId IS NULL OR i.monitor.id=:monitorId)
                AND (CAST(:from AS timestamp) IS NULL OR  i.startedAt >= :from)
                AND(CAST(:to AS timestamp) IS NULL OR i.startedAt <= :to)
            """)
    Page<Incident> findAllIncidents(
           @Param("tenantId") UUID tenantId,
           @Param("status") IncidentStatus status,
           @Param("monitorId") UUID monitorId,
           @Param("from") LocalDateTime from,
           @Param("to") LocalDateTime to,
            Pageable pageable);

    Optional<Incident> findByIdAndTenantId(UUID incidentId, UUID tenantId);
}
