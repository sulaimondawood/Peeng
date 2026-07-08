package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.models.IncidentDiagnosticTrace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IncidentDiagnosticRepository extends JpaRepository<IncidentDiagnosticTrace, UUID> {
}
