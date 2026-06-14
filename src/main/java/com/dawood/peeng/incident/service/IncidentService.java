package com.dawood.peeng.incident.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.exceptions.IncidentNotFoundException;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.monitor.models.Monitor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public Incident openIncident(Monitor monitor) {

        boolean alreadyOpen =
                incidentRepository.existsByMonitor_IdAndStatus(
                        monitor.getId(),
                        IncidentStatus.OPEN
                );

        if (alreadyOpen) {
            return null;
        }


        Incident newIncident = Incident.builder()
                .monitor(monitor)
                .tenant(monitor.getTenant())
                .status(IncidentStatus.OPEN)
                .startedAt(LocalDateTime.now())
                .latestErrorMessage(monitor.getLatestErrorMessage())
                .failureCount(monitor.getConsecutiveFailures())
                .initialStatusCode(monitor.getLatestStatusCode())
                .initialResponseTimeMs(monitor.getLatestResponseTimeMs())
                .acknowledged(false)
                .build();

        return incidentRepository.save(newIncident);

    }

    public Incident resolveIncident(Monitor monitor) {

        long startTime = System.currentTimeMillis();

        LocalDateTime now =LocalDateTime.now();

        Incident incident =
                incidentRepository
                        .findByMonitor_IdAndStatus(
                                monitor.getId(),
                                IncidentStatus.OPEN
                        )
                        .orElseThrow(() -> new IncidentNotFoundException(
                                "Incident does not exist",
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

        incident.setResolvedStatusCode(
                HttpStatus.OK.value()
        );

        incident.setResolvedResponseTimeMs(
                System.currentTimeMillis() - startTime
        );

        return incidentRepository.save(incident);
    }
}
