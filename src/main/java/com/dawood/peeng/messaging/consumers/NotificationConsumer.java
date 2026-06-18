package com.dawood.peeng.messaging.consumers;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.messaging.events.IncidentEvent;
import com.dawood.peeng.messaging.mails.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @Value("{app.client-url}")
    private String clientUrl;

    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy, hh:mm a");

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
                "responseTime", event.getResponseTimeMS(),
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

        } catch (Exception e) {
            log.error("Failed to process or send email notification for incident ID: #{}", event.getIncidentId(), e);
            throw e;
        }


    }

    @RabbitListener(queues = RabbitMQConfig.INCIDENT_CLOSED_QUEUE)
    public void consumeIncidentResolvedNotification(IncidentEvent event) {

        log.info("Received incident resolved event for monitor: {}", event.getMonitorName());

        Context ctx = new Context();

        String formattedStart = event.getStartedAt() != null
                ? event.getStartedAt().format(DATE_TIME_FORMATTER) : "-";

        String formattedResolved = event.getResolvedAt() != null
                ? event.getResolvedAt().format(DATE_TIME_FORMATTER) : "-";

        Map<String, Object> variables = Map.ofEntries(
                Map.entry("logoUrl", clientUrl + "/logo.png"),
                Map.entry("workspaceName", event.getWorkspaceName()),
                Map.entry("monitorName", event.getMonitorName()),
                Map.entry("monitorUrl", event.getMonitorUrl()),
                Map.entry("incidentId", event.getIncidentId()),
                Map.entry("startedAt", formattedStart),
                Map.entry("resolvedAt", formattedResolved),
                Map.entry("downtime", formatDuration(event.getDurationSeconds())),
                Map.entry("resolvedStatusCode", event.getStatusCode()),
                Map.entry("resolvedResponseTime", event.getResponseTimeMS()),
                Map.entry("dashboardUrl", event.getDashboardIncidentUrl())
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

}
