package com.dawood.peeng.incident.repository;

import com.dawood.peeng.incident.enums.DeliveryStatus;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.models.IncidentNotificationTrace;
import com.dawood.peeng.notification.enums.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IncidentNotificationTraceRepository extends JpaRepository<IncidentNotificationTrace, UUID> {


    Optional<IncidentNotificationTrace> findByIncidentIdAndChannelAndStatus(UUID incidentId, NotificationChannel channel, DeliveryStatus status);

    Optional<IncidentNotificationTrace> findByIncidentAndChannelAndTargetChannelName(Incident incident, NotificationChannel channel, String target);
}
