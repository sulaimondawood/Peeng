package com.dawood.peeng.monitor.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.dto.response.CheckResult;
import com.dawood.peeng.incident.events.IncidentOpenedEvent;
import com.dawood.peeng.incident.events.IncidentResolvedEvent;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.service.IncidentService;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.models.MonitorCheck;
import com.dawood.peeng.monitor.repository.MonitorCheckRepository;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import com.dawood.peeng.tenant.exceptions.TenantException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MonitorCheckService {

    private static final Logger log = LoggerFactory.getLogger(MonitorCheckService.class);
    private final MonitorCheckRepository monitorCheckRepository;
    private final MonitorRepository monitorRepository;
    private final TenantRepository tenantRepository;
    private final IncidentService incidentService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void processSuccess(Monitor monitor, UUID tenantId, CheckResult result) {

        long responseTime = System.currentTimeMillis() - result.startTime();
        LocalDateTime now = LocalDateTime.now();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Tenant does not exists",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        int statusCode = Optional.ofNullable(result.response())
                .map(res -> res.getStatusCode().value())
                .orElse(0);

        MonitorCheck monitorCheck = MonitorCheck.builder()
                .monitor(monitor)
                .tenant(tenant)
                .successful(true)
                .statusCode(statusCode)
                .responseTimeMs(responseTime)
                .checkedAt(LocalDateTime.now())
                .build();

        monitorCheckRepository.save(monitorCheck);

        if (result.isHighLatency()) {
            log.warn("Monitor {} responded with 200 OK but breached latency limits.", monitor.getName());
            this.processFailure(monitor, tenantId, result);
            return;
        }

        monitor.setNextCheckAt(
                now.plusSeconds(monitor.getIntervalInSeconds())
        );
        monitor.setConsecutiveFailures(0);
        monitor.setConsecutiveSuccesses(
                monitor.getConsecutiveSuccesses() + 1);
        monitor.setLatestStatusCode(statusCode);
        monitor.setLatestResponseTimeMs(responseTime);
        monitor.setLastCheckedAt(LocalDateTime.now());
        monitor.setLastSuccessfulCheckAt(LocalDateTime.now());
        monitor.setLatestErrorMessage(null);


        boolean needsRecovery = monitor.getStatus() == MonitorStatus.DOWN
                || monitor.getStatus() == MonitorStatus.PENDING;

        if (needsRecovery && monitor.getConsecutiveSuccesses() >= monitor.getRecoveryThreshold()) {
            boolean wasIncidentOpen = monitor.isIncidentOpen();

            monitor.setStatus(MonitorStatus.UP);
            monitor.setLastStatusChangeAt(LocalDateTime.now());
            monitor.setIncidentOpen(false);

            Incident resolvedIncident = null;
            if (wasIncidentOpen) {
                resolvedIncident = incidentService.resolveIncident(monitor);
            }

            monitorRepository.save(monitor);

            if (resolvedIncident != null) {
                log.info("Firing recovery notification alert for monitor: {}", monitor.getName());
                applicationEventPublisher.publishEvent(new IncidentResolvedEvent(resolvedIncident.getId()));
            }
            return;
        }

        log.info("Monitor {} passed health check. Progressing toward recovery threshold ({}).",
                monitor.getName(), monitor.getConsecutiveSuccesses());

        monitorRepository.save(monitor);
    }

    @Transactional
    public void processFailure(
            Monitor monitor,
            UUID tenantId,
            CheckResult result
    ) {
        long responseTime = System.currentTimeMillis() - result.startTime();
        LocalDateTime now = LocalDateTime.now();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Tenant does not exists",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        Integer statusCode = Optional.ofNullable(result.response())
                .map((res) -> res.getStatusCode().value())
                .orElse(0);

        MonitorCheck monitorCheck = MonitorCheck.builder()
                .monitor(monitor)
                .tenant(tenant)
                .successful(false)
                .statusCode(statusCode)
                .responseTimeMs(responseTime)
                .errorMessage(result.message())
                .checkedAt(LocalDateTime.now())
                .build();

        monitorCheckRepository.save(monitorCheck);

        monitor.setNextCheckAt(
                now.plusSeconds(monitor.getIntervalInSeconds()));
        monitor.setConsecutiveFailures(monitor.getConsecutiveFailures() + 1);
        monitor.setConsecutiveSuccesses(
                0);
        monitor.setLatestStatusCode(statusCode);
        monitor.setLatestResponseTimeMs(responseTime);
        monitor.setLastCheckedAt(LocalDateTime.now());
        monitor.setLatestErrorMessage(result.isHighLatency() && result.message() == null ?
                String.format("High latency detected: Response took %.2f secs", responseTime / 1000.0) :
                result.message()
        );


        if (monitor.getConsecutiveFailures() >= monitor.getFailureThreshold()) {
            if (!monitor.isIncidentOpen()) {
                log.warn("Monitor {} breached failure threshold. Transitioning to DOWN.", monitor.getName());

                monitor.setStatus(MonitorStatus.DOWN);
                monitor.setLastStatusChangeAt(
                        LocalDateTime.now());
                monitor.setIncidentOpen(true);

                monitorRepository.save(monitor);

                Incident openedIncident = incidentService.openIncident(monitor, result);
                log.info("Firing fresh incident alert for monitor: {}", monitor.getName());
                applicationEventPublisher.publishEvent(new IncidentOpenedEvent(openedIncident.getId()));
            }
            return;

        }

        if (monitor.getStatus() == MonitorStatus.UP) {
            log.info("Monitor {} is degrading. Transitioning to PENDING state.", monitor.getName());

            monitor.setStatus(MonitorStatus.PENDING);
            monitor.setLastStatusChangeAt(LocalDateTime.now());
        }
            monitorRepository.save(monitor);

    }


}
