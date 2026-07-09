package com.dawood.peeng.incident.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.BadRequestException;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.exceptions.UnauthorizedException;
import com.dawood.peeng.identity.exceptions.UserNotFoundException;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.incident.dto.request.IncidentFilterRequest;
import com.dawood.peeng.incident.dto.response.CheckResult;
import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.dto.response.IncidentOverview;
import com.dawood.peeng.incident.enums.ActivityType;
import com.dawood.peeng.incident.enums.DateRangeBucket;
import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.enums.Severity;
import com.dawood.peeng.incident.events.IncidentAssignedEvent;
import com.dawood.peeng.incident.exceptions.IncidentNotFoundException;
import com.dawood.peeng.incident.mapper.IncidentMapper;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.models.IncidentDiagnosticTrace;
import com.dawood.peeng.incident.repository.IncidentDiagnosticRepository;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.membership.enums.MembershipStatus;
import com.dawood.peeng.membership.exceptions.MembershipException;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.service.MonitorService;
import com.dawood.peeng.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final MonitorService monitorService;
    private final IncidentActivityLogService incidentActivityLogService;
    private final RestClient.Builder restBuilder;
    private final UserRepository userRepository;
    private final IncidentDiagnosticRepository incidentDiagnosticRepository;
    private final MembershipRepository membershipRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    public Incident openIncident(Monitor monitor, CheckResult result) {

        Optional<Incident> existingIncident =
                incidentRepository.findByMonitor_IdAndStatus(
                        monitor.getId(),
                        IncidentStatus.OPEN
                );

        if (existingIncident.isPresent()) {
            return existingIncident.get();
        }

        int statusCode = result.response() != null ? result.response().getStatusCode().value() : 0;

        boolean isNetworkOrAppCrash = result.isTimeout() || (statusCode >= 400);
        Severity severity = isNetworkOrAppCrash ? Severity.CRITICAL :
                result.isHighLatency() ? Severity.WARNING :
                        Severity.INFO;

        ActivityType activityType = switch (severity) {
            case CRITICAL -> ActivityType.CRITICAL;
            case WARNING -> ActivityType.WARNING;
            default -> ActivityType.DIAGNOSTIC;
        };

        Incident newIncident = Incident.builder()
                .monitor(monitor)
                .tenant(monitor.getTenant())
                .status(IncidentStatus.OPEN)
                .startedAt(LocalDateTime.now())
                .latestErrorMessage(monitor.getLatestErrorMessage())
                .failureCount(monitor.getConsecutiveFailures())
                .initialStatusCode(monitor.getLatestStatusCode())
                .initialResponseTimeMs(monitor.getLatestResponseTimeMs())
                .severity(severity)
                .acknowledged(false)
                .build();

        Incident savedIncident = incidentRepository.save(newIncident);

        incidentActivityLogService.logActivity(
                savedIncident,
                activityType,
                "Incident threshold triggered",
                savedIncident.getLatestErrorMessage()
        );

        return savedIncident;

    }

    public Incident resolveIncident(Monitor monitor) {
        LocalDateTime now = LocalDateTime.now();

        Incident incident =
                incidentRepository
                        .findByMonitor_IdAndStatus(
                                monitor.getId(),
                                IncidentStatus.OPEN
                        )
                        .orElseThrow(() -> new IncidentNotFoundException(
                                "Open Incident does not exist",
                                HttpStatus.NOT_FOUND,
                                ErrorCode.NOT_FOUND));

        incident.setResolvedAt(now);
        incident.setStatus(IncidentStatus.RESOLVED);
        incident.setDurationSeconds(
                Duration.between(
                        incident.getStartedAt(),
                        now
                ).toSeconds()
        );
        incident.setResolvedStatusCode(monitor.getLatestStatusCode()
        );
        incident.setResolvedResponseTimeMs(monitor.getLatestResponseTimeMs());

        Incident savedIncident = incidentRepository.save(incident);

        incidentActivityLogService.logActivity(
                savedIncident,
                ActivityType.RECOVERY,
                "Incident threshold triggered",
                "Automatic service restoration: Monitor recovered status back to UP (200 OK) with stable metrics."
        );

        return savedIncident;
    }

    public List<IncidentDTO> recentIncident(UUID monitorId) {
        final UUID tenantId = TenantContext.getTenantId();
        monitorService.validateMonitorAccess(monitorId, tenantId);

        return incidentRepository.findTop10ByMonitorIdAndTenantIdOrderByStartedAtDesc(monitorId, tenantId)
                .stream()
                .map(IncidentMapper::toDTO)
                .toList();
    }

    public List<IncidentDTO> getActiveIncidents() {
        final UUID tenantId = TenantContext.getTenantId();

        return incidentRepository.findByTenantIdAndStatus(tenantId, IncidentStatus.OPEN)
                .stream()
                .map(IncidentMapper::toDTO)
                .toList();


    }

    public Page<Incident> getAllIncidents(IncidentFilterRequest request) {

        UUID tenantId = TenantContext.getTenantId();
        Pageable pageable = PageRequest.of(request.page(), request.size());

        IncidentStatus status = IncidentStatus.fromString(request.status());

        DateRangeBucket rangeBucket = DateRangeBucket.fromString(request.dateBucket());

        LocalDateTime to = null;
        LocalDateTime from = null;

        if (rangeBucket != null) {
            to = LocalDateTime.now();
            from = rangeBucket.getFromLocalDateTime(to);
        }

        return incidentRepository.findAllIncidents(tenantId, status, request.monitorId(), from, to, pageable);

    }

    public IncidentOverview getIncidentDetails(UUID incidentId) {

        UUID tenantId = TenantContext.getTenantId();

        Incident incident = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incident not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        return IncidentMapper.toIncidentOverview(incident);

    }

    public IncidentDiagnosticTrace executeManualManualHandshake(UUID incidentId) {

        UUID tenantId = TenantContext.getTenantId();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException("User not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        Incident incident = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incident not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        Monitor scheduledMonitor = incident.getMonitor();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(scheduledMonitor.getTimeoutInSeconds()));
        factory.setReadTimeout(Duration.ofSeconds(scheduledMonitor.getTimeoutInSeconds()));

        long startTIme = 0L;
        long responseTimeMs = 0L;
        ResponseEntity<Void> response = null;
        try {

            startTIme = System.currentTimeMillis();
            response = restBuilder.requestFactory(factory)
                    .build()
                    .get()
                    .uri(scheduledMonitor.getUrl())
                    .retrieve()
                    .toBodilessEntity();

            responseTimeMs = System.currentTimeMillis() - startTIme;

            IncidentDiagnosticTrace diagnosticTrace = new IncidentDiagnosticTrace();
            diagnosticTrace.setIncident(incident);
            diagnosticTrace.setTriggeredBy(user);
            diagnosticTrace.setStatusCode(response.getStatusCode().value());
            diagnosticTrace.setResponseTimeMs(responseTimeMs);
            diagnosticTrace.setSuccessful(true);
            diagnosticTrace.setMessage("Handshake completed safely from primary hub node.");
            incidentDiagnosticRepository.save(diagnosticTrace);

            incidentActivityLogService.logActivity(
                    incident,
                    ActivityType.DIAGNOSTIC,
                    "Diagnostic Closure Handshake",
                    "All verification handshakes succeeded. Performance baseline returned to stable. Monitoring triggers calibrated for active surveilance. Automated incident trace successfully archived"
            );

            return diagnosticTrace;

        } catch (Exception ex) {
            responseTimeMs = System.currentTimeMillis() - startTIme;
            int code = (response != null) ? response.getStatusCode().value() : 0;

            IncidentDiagnosticTrace diagnosticTrace = new IncidentDiagnosticTrace();
            diagnosticTrace.setIncident(incident);
            diagnosticTrace.setTriggeredBy(user);
            diagnosticTrace.setStatusCode(code);
            diagnosticTrace.setResponseTimeMs(responseTimeMs);
            diagnosticTrace.setSuccessful(false);
            diagnosticTrace.setMessage("Trace failed: " + ex.getMessage());
            incidentDiagnosticRepository.save(diagnosticTrace);

            incidentActivityLogService.logActivity(
                    incident,
                    ActivityType.DIAGNOSTIC,
                    "Diagnostic Closure Handshake",
                    "All verification handshakes failed. Performance baseline returned to unstable. Automated incident trace successfully archived"
            );

            return diagnosticTrace;
        }


    }

    public void assignTeamMemberToIncident(UUID incidentId, UUID memberId) {

        UUID tenantId = TenantContext.getTenantId();
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Membership assigner = membershipRepository.findByUser_EmailAndTenant_Id(email, tenantId)
                .orElseThrow(() -> new UserNotFoundException("User not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        Membership assignee = membershipRepository.findByIdAndTenantIdAndStatus(memberId, tenantId, MembershipStatus.ACTIVE)
                .orElseThrow(() -> new MembershipException("Target member not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        boolean isPrivileged = assigner.getRole() == RoleType.ADMIN || assigner.getRole() == RoleType.OWNER;

        if (!isPrivileged) {
            if ((assigner.getId() == assignee.getId())) {
                assignTeamMember(tenantId, incidentId, assignee, assigner);
                return;
            }
            throw new UnauthorizedException(
                    "You're not authorized to assign member to incident",
                    HttpStatus.UNAUTHORIZED,
                    ErrorCode.UNAUTHORIZED
            );
        }
        assignTeamMember(tenantId, incidentId, assignee, assigner);

    }

    private void assignTeamMember(UUID tenantId, UUID incidentId, Membership assignee, Membership assigner) {
        Incident incident = incidentRepository.findByIdAndTenantId(incidentId, tenantId)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incident not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        if (incident.getStatus() != IncidentStatus.RESOLVED) {
            incident.setAcknowledgedAt(LocalDateTime.now());
            incident.setAcknowledged(true);
            incident.setAcknowledgedBy(assignee.getUser());

            String assigneeName = assignee.getUser().getName();
            String assignerName = assigner.getUser().getName();
            String assigneeEmail = assignee.getUser().getEmail();

            String message = String.format("Incident assigned to %s by %s", assigneeName, assignerName);

            incidentActivityLogService.logActivity(
                    incident,
                    ActivityType.ACKNOWLEDGEMENT,
                    "On-Call Assignment Calibrated",
                    message);

            applicationEventPublisher.publishEvent(new IncidentAssignedEvent(
                    incident.getId(),
                    incident.getMonitor().getId(),
                    tenantId,
                    assignerName,
                    assigneeName,
                    assigneeEmail
            ));

        } else {
            throw new BadRequestException("Cannot assign closed incidents", HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }
    }
}
