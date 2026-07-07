package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.models.IncidentNotificationTrace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IncidentNotificationTraceRepository extends JpaRepository<IncidentNotificationTrace, UUID> {
}
