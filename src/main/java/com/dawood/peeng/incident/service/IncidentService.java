package com.dawood.peeng.incident.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.dto.request.IncidentFilterRequest;
import com.dawood.peeng.incident.dto.response.CheckResult;
import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.dto.response.IncidentOverview;
import com.dawood.peeng.incident.enums.ActivityType;
import com.dawood.peeng.incident.enums.DateRangeBucket;
import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.enums.Severity;
import com.dawood.peeng.incident.exceptions.IncidentNotFoundException;
import com.dawood.peeng.incident.mapper.IncidentMapper;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.service.MonitorService;
import com.dawood.peeng.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

}
