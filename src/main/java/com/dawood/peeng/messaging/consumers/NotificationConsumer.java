package com.dawood.peeng.messaging.consumers;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.incident.enums.ActivityType;
import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.models.IncidentActivity;
import com.dawood.peeng.incident.repository.IncidentActivityRepository;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.incident.service.IncidentActivityLogService;
import com.dawood.peeng.incident.service.IncidentService;
import com.dawood.peeng.messaging.events.IncidentEvent;
import com.dawood.peeng.messaging.mails.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final TemplateEngine templateEngine;
    private final EmailService emailService;
    private final IncidentRepository incidentRepository;
    private final IncidentActivityLogService activityLogService;

    @Value("${app.client-url}")
    private String clientUrl;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

    @RabbitListener(queues = RabbitMQConfig.INCIDENT_OPENED_QUEUE)
    public void consumeIncidentOpenedNotification(IncidentEvent event) {
        Context ctx = new Context();

        Map<String, Object> variables = Map.of(
                "logoUrl", clientUrl + "/logo.png",
                "workspaceName", event.getWorkspaceName(),
                "monitorName", event.getMonitorName(),
                "monitorUrl", event.getMonitorUrl(),
                "incidentId", event.getIncidentId(),
                "statusCode", event.getStatusCode(),
                "responseTime", formatDurationMS(event.getResponseTimeMS()),
                "failureCount", event.getFailureCount(),
                "latestError", event.getErrorMessage(),
                "dashboardUrl", event.getDashboardIncidentUrl()
        );

        ctx.setVariables(variables);

        String subject = String.format("[Peeng] Incident Opened: [%s] %s is DOWN (%d)",
                event.getWorkspaceName(),
                event.getMonitorName(),
                event.getStatusCode()
        );

        try {
            String body = templateEngine.process("incident-opened", ctx);
            emailService.send(event.getDestination(), subject, body);
            log.info("Successfully sent incident email notification to {}", event.getDestination());

            incidentRepository.findById(
                    event.getIncidentId()).ifPresent(existingIncident -> activityLogService.logActivity(
                    existingIncident,
                    ActivityType.ESCALATION,
                    "Alert Dispatched",
                    "Outage notifications successfully dispatched to channel: " + event.getDestination()
            ));

        } catch (Exception e) {
            log.error("Failed to process or send email notification for incident ID: #{}", event.getIncidentId(), e);
            throw e;
        }

    }

    @RabbitListener(queues = RabbitMQConfig.INCIDENT_CLOSED_QUEUE)
    public void consumeIncidentResolvedNotification(IncidentEvent event) {
        Context ctx = new Context();

        String formattedStart = formatEventTimestamp(event.getStartedAt());
        String formattedResolved = formatEventTimestamp(event.getResolvedAt());

        Map<String, Object> variables = new HashMap<>();
        variables.put("logoUrl", clientUrl + "/logo.png");
        variables.put("workspaceName", event.getWorkspaceName());
        variables.put("monitorName", event.getMonitorName());
        variables.put("monitorUrl", event.getMonitorUrl());
        variables.put("incidentId", event.getIncidentId());
        variables.put("startedAt", formattedStart);
        variables.put("resolvedAt", formattedResolved);
        variables.put("downtime", formatDuration(event.getDurationSeconds()));
        variables.put("resolvedStatusCode", event.getStatusCode());
        variables.put("resolvedResponseTime", formatDurationMS(event.getResponseTimeMS()));
        variables.put("dashboardUrl", event.getDashboardIncidentUrl());

        ctx.setVariables(variables);

        String subject = String.format("[Peeng] Incident Resolved: [%s] %s is BACK ONLINE",
                event.getWorkspaceName(),
                event.getMonitorName()
        );

        try {
            String body = templateEngine.process("incident-resolved", ctx);
            emailService.send(event.getDestination(), subject, body);
            log.info("Successfully sent incident recovery email notification to {}", event.getDestination());

        } catch (Exception e) {
            log.error("Failed to process or send email notification for incident ID: #{}", event.getIncidentId(), e);
            throw e;
        }


    }

    private String formatDuration(Long totalSeconds) {
        if (totalSeconds == null || totalSeconds == 0) return "0s";
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        if (minutes == 0) return seconds + "s";
        return String.format("%dm %ds", minutes, seconds);
    }

    private String formatDurationMS(Long totalMs) {
        if (totalMs == null || totalMs == 0) return "0ms";

        long minutes = totalMs / 60000;
        long remainderAfterMinutes = totalMs % 60000;

        long seconds = remainderAfterMinutes / 1000;
        long milliseconds = remainderAfterMinutes % 1000;

        StringBuilder result = new StringBuilder();

        if (minutes > 0) {
            result.append(minutes).append("m ");
        }
        if (seconds > 0 || minutes > 0) {
            result.append(seconds).append("s ");
        }
        if (milliseconds > 0 || result.length() == 0) {
            result.append(milliseconds).append("ms");
        }

        return result.toString().trim();
    }

    private String formatEventTimestamp(String isoTimestampString) {
        if (isoTimestampString == null || isoTimestampString.isBlank()) {
            return "-";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoTimestampString, ISO_FORMATTER);
            return dateTime.format(DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("Failed to parse timestamp string: {}", isoTimestampString);
            return isoTimestampString; // Fallback directly to the raw text if parsing hits an issue
        }
    }

}
