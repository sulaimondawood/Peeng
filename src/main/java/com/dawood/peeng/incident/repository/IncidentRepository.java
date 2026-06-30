package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.models.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    long incidentCount(@Param("tenantId") UUID tenantId, @Param("monitorId") UUID monitorId);;
}
