package com.dawood.peeng.monitor.service;

import java.time.LocalDateTime;

import com.dawood.peeng.incident.events.IncidentOpenedEvent;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.service.IncidentService;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.models.Monitor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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

    public void handleFailure(Monitor monitor) {

        if (monitor.getConsecutiveFailures() >= monitor.getFailureThreshold()) {

            if (monitor.getStatus() != MonitorStatus.DOWN) {

                monitor.setStatus(MonitorStatus.DOWN);
                monitor.setLastStatusChangeAt(
                        LocalDateTime.now());
                monitor.setIncidentOpen(true);

                monitorRepository.save(monitor);

            }

            Incident openedIncident = incidentService.openIncident(monitor);

            applicationEventPublisher.publishEvent(new IncidentOpenedEvent(openedIncident.getId()));


        }

    }

}
