package com.dawood.peeng.monitor.service;

import java.time.LocalDateTime;

import com.dawood.peeng.incident.events.IncidentOpenedEvent;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.service.IncidentService;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.models.Monitor;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorStateService {

    private final IncidentService incidentService;
    private final MonitorRepository monitorRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void handleSuccess(Monitor monitor) {

        boolean needsRecovery = monitor.getStatus() == MonitorStatus.DOWN
                || monitor.getStatus() == MonitorStatus.PENDING;

        if (needsRecovery
                && monitor.getConsecutiveSuccesses() >= monitor.getRecoveryThreshold()) {

            monitor.setStatus(MonitorStatus.UP);
            monitor.setLastStatusChangeAt(LocalDateTime.now());
            monitor.setIncidentOpen(false);

            monitorRepository.save(monitor);

        }

    }

    @Transactional
    public void handleFailure(Monitor monitor) {

        if (monitor.getConsecutiveFailures() >= monitor.getFailureThreshold()) {
            boolean wasAlreadyDown = monitor.getStatus() == MonitorStatus.DOWN;

            if(!wasAlreadyDown){
                log.warn("Monitor {} breached failure threshold. Transitioning to DOWN.", monitor.getName());

                monitor.setStatus(MonitorStatus.DOWN);
                monitor.setLastStatusChangeAt(
                        LocalDateTime.now());
                monitor.setIncidentOpen(true);

                monitorRepository.save(monitor);

            }

            Incident openedIncident = incidentService.openIncident(monitor);

            if (!wasAlreadyDown && openedIncident != null) {
                log.info("Firing fresh incident alert for monitor: {}", monitor.getName());
                applicationEventPublisher.publishEvent(new IncidentOpenedEvent(openedIncident.getId()));

            }


        }else if (monitor.getStatus() == MonitorStatus.UP) {
            log.info("Monitor {} is degrading. Transitioning to PENDING state.", monitor.getName());

            monitor.setStatus(MonitorStatus.PENDING);
            monitor.setLastStatusChangeAt(LocalDateTime.now());
            monitorRepository.save(monitor);

        }

    }

}
