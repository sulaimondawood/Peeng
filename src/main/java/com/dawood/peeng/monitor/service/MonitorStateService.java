package com.dawood.peeng.monitor.service;

import com.dawood.peeng.incident.events.IncidentOpenedEvent;
import com.dawood.peeng.incident.events.IncidentResolvedEvent;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.service.IncidentService;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorStateService {

    private final IncidentService incidentService;
    private final MonitorRepository monitorRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSuccess(Monitor monitor) {
        boolean needsRecovery = monitor.getStatus() == MonitorStatus.DOWN
                || monitor.getStatus() == MonitorStatus.PENDING;

        if (needsRecovery && monitor.getConsecutiveSuccesses() >= monitor.getRecoveryThreshold()) {
            boolean wasIncidentOpen = monitor.isIncidentOpen();

            monitor.setStatus(MonitorStatus.UP);
            monitor.setLastStatusChangeAt(LocalDateTime.now());
            monitor.setIncidentOpen(false);

            monitorRepository.saveAndFlush(monitor);

            Incident resolvedIncident = null;
            if (wasIncidentOpen) {
                resolvedIncident = incidentService.resolveIncident(monitor);
            }

            if (resolvedIncident != null) {
                log.info("Firing recovery notification alert for monitor: {}", monitor.getName());
                applicationEventPublisher.publishEvent(new IncidentResolvedEvent(resolvedIncident.getId()));
            }
            return;
        }

        log.info("Monitor {} passed health check. Progressing toward recovery threshold ({}).",
                monitor.getName(), monitor.getConsecutiveSuccesses());
        monitorRepository.saveAndFlush(monitor);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailure(Monitor monitor) {

        if (monitor.getConsecutiveFailures() >= monitor.getFailureThreshold()) {
            if (!monitor.isIncidentOpen()) {
                log.warn("Monitor {} breached failure threshold. Transitioning to DOWN.", monitor.getName());

                monitor.setStatus(MonitorStatus.DOWN);
                monitor.setLastStatusChangeAt(
                        LocalDateTime.now());
                monitor.setIncidentOpen(true);

                monitorRepository.save(monitor);

                Incident openedIncident = incidentService.openIncident(monitor);
                log.info("Firing fresh incident alert for monitor: {}", monitor.getName());
                applicationEventPublisher.publishEvent(new IncidentOpenedEvent(openedIncident.getId()));
            }
            return;

        }

        if (monitor.getStatus() == MonitorStatus.UP) {
            log.info("Monitor {} is degrading. Transitioning to PENDING state.", monitor.getName());

            monitor.setStatus(MonitorStatus.PENDING);
            monitor.setLastStatusChangeAt(LocalDateTime.now());
            monitorRepository.save(monitor);

        }

    }

}
