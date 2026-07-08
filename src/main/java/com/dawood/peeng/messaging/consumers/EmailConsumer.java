package com.dawood.peeng.messaging.consumers;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.events.IncidentAssignedEvent;
import com.dawood.peeng.incident.exceptions.IncidentNotFoundException;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.messaging.events.SendVerificationEmailEvent;
import com.dawood.peeng.messaging.mails.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final EmailService emailService;
    private final TemplateEngine templateEngine;
    private final IncidentRepository incidentRepository;

    @Value("${app.client-url}")
    private String clientUrl;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void consumeVerificationEmail(SendVerificationEmailEvent event) {

        log.info("Received email verification event for: {}", event.getEmail());
        try {

            String activationLink = String.format("%s/auth/verify?token=%s", clientUrl, event.getToken());

            Context context = new Context();
            context.setVariable("name", event.getName());
            context.setVariable("expiresIn", "24hrs");
            context.setVariable("activationLink", activationLink);

            String body = templateEngine.process("email-verification", context);

            emailService.send(event.getEmail(), "Account verification - Peeng", body);

        } catch (Exception e) {
            log.error("Error processing email template or sending message", e);
            throw e;
        }

    }

    @RabbitListener(queues = RabbitMQConfig.INCIDENT_ASSIGNED_TO_QUEUE)
    public void consumeIncidentAssignedNotification(IncidentAssignedEvent event) {

        log.info("Received Incident Assigned Event to: {}", event.getAssignee());

        Incident incident = incidentRepository.findByIdAndTenantIdAndMonitorId(
                event.getIncidentAssignedId(),
                event.getTenantId(),
                event.getMonitorId()
        ).orElseThrow(() -> new IncidentNotFoundException(
                "Incident does not exist",
                HttpStatus.NOT_FOUND,
                ErrorCode.NOT_FOUND
        ));

        String monitorUrl = incident.getMonitor().getUrl();
        String monitorName = incident.getMonitor().getName();
        String dashboardUrl = String.format(clientUrl + "/dashboard/incident/%s", event.getIncidentAssignedId());

        Context context = new Context();
        context.setVariable("logoUrl", clientUrl + "/logo.png");
        context.setVariable("assigneeName", event.getAssignee());
        context.setVariable("incidentId", event.getIncidentAssignedId());
        context.setVariable("monitorName", monitorName);
        context.setVariable("monitorUrl", monitorUrl);
        context.setVariable("assignedBy", event.getAssigner());
        context.setVariable("dashboardUrl", dashboardUrl);

        try {
            String body = templateEngine.process("incident-assigned", context);
            emailService.send(event.getEmail(), "Incident Assignment Notification - Peeng", body);

        } catch (Exception e) {
            log.error("Error processing incident assigned email template or sending message", e);
            throw e;
        }

    }
}
