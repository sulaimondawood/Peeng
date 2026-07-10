package com.dawood.peeng.notification.service;

import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.messaging.events.IncidentEvent;
import com.dawood.peeng.messaging.producers.NotificationProducer;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.notification.model.NotificationChannelConfig;
import com.dawood.peeng.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class EmailNotificationProvider implements NotificationProvider {

    private final NotificationProducer notificationProducer;

    @Value("${app.client-url}")
    private String frontendUrl;


    @Override
    public void sendDownAlert(Incident incident, NotificationChannelConfig config) {

        UUID tenantId = TenantContext.getTenantId();

        Monitor monitor = incident.getMonitor();

        IncidentEvent event = IncidentEvent.builder()
                .workspaceName(incident.getTenant().getWorkspaceName())
                .monitorName(monitor.getName())
                .monitorUrl(monitor.getUrl())
                .monitorId(monitor.getId())
                .tenantId(tenantId)
                .incidentId(incident.getId())
                .statusCode(incident.getInitialStatusCode())
                .responseTimeMS(incident.getInitialResponseTimeMs())
                .failureCount(incident.getFailureCount())
                .errorMessage(Optional.ofNullable(incident.getLatestErrorMessage()).orElse("The monitor failed its health check."))
                .year(Year.now().getValue())
                .destination(config.getDestination())
                .startedAt(incident.getStartedAt().toString())
                .dashboardIncidentUrl(frontendUrl+"/dashboard/incidents/"+incident.getId())
                .durationSeconds(incident.getDurationSeconds())
                .build();

        notificationProducer.sendIncidentMail(event);

    }

    @Override
    public void sendRecoveryAlert(Incident incident, NotificationChannelConfig config) {

        Monitor monitor = incident.getMonitor();
        UUID tenantId = TenantContext.getTenantId();


        IncidentEvent event = IncidentEvent.builder()
                .workspaceName(incident.getTenant().getWorkspaceName())
                .monitorName(monitor.getName())
                .monitorUrl(monitor.getUrl())
                .incidentId(incident.getId())
                .tenantId(tenantId)
                .statusCode(incident.getInitialStatusCode())
                .responseTimeMS(incident.getInitialResponseTimeMs())
                .failureCount(incident.getFailureCount())
                .errorMessage(Optional.ofNullable(incident.getLatestErrorMessage()).orElse("The monitor failed its health check."))
                .year(Year.now().getValue())
                .destination(config.getDestination())
                .startedAt(incident.getStartedAt().toString())
                .dashboardIncidentUrl(frontendUrl+"/dashboard/incidents/"+incident.getId())
                .resolvedAt(incident.getResolvedAt().toString())
                .durationSeconds(incident.getDurationSeconds())
                .build();

        notificationProducer.sendIncidentResolvedMail(event);

    }
}
