package com.dawood.peeng.notification.service;

import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.messaging.events.IncidentEvent;
import com.dawood.peeng.messaging.producers.NotificationProducer;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.notification.model.NotificationChannelConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;

@RequiredArgsConstructor
@Service
public class EmailNotificationProvider implements NotificationProvider {

    private final NotificationProducer notificationProducer;

    @Override
    public void sendDownAlert(Incident incident, NotificationChannelConfig config) {

        Monitor monitor = incident.getMonitor();

        IncidentEvent event = IncidentEvent.builder()
                .workspaceName(incident.getTenant().getWorkspaceName())
                .monitorName(monitor.getName())
                .monitorUrl(monitor.getName())
                .incidentId(incident.getId().toString())
                .statusCode(incident.getInitialStatusCode())
                .responseTimeMS(incident.getInitialResponseTimeMs())
                .failureCount(incident.getFailureCount())
                .errorMessage(incident.getLatestErrorMessage())
                .year(Year.now().getValue())
                .destination(config.getDestination())
                .build();

        notificationProducer.sendIncidentMail(event);

    }

    @Override
    public void sendRecoveryAlert(Incident incident, NotificationChannelConfig config) {

    }
}
