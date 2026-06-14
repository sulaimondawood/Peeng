package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.models.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    boolean existsByMonitor_IdAndStatus(UUID monitorId, IncidentStatus status);

    Optional<Incident> findByMonitor_IdAndStatus(UUID monitorId, IncidentStatus status);
}
