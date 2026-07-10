package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.enums.ActivityType;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.models.IncidentActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentActivityRepository extends JpaRepository<IncidentActivity, UUID> {

    Optional<IncidentActivity> findByIncidentId(UUID incidentId);

    boolean existsByIncidentAndTypeAndTitle(Incident incident, ActivityType type, String messageSnippet);

    List<IncidentActivity> findByIncidentIdAndTenantId(UUID incidentId, UUID tenantId);
}
