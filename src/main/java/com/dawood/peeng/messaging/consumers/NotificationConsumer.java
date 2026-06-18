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

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final TemplateEngine templateEngine;
    private final EmailService emailService;

    @Value("{app.client-url}")
    private String clientUrl;

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

}
