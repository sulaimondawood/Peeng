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
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    @Transactional()
    public Incident openIncident(Monitor monitor) {

        Optional<Incident> existingIncident =
                incidentRepository.findByMonitor_IdAndStatus(
                        monitor.getId(),
                        IncidentStatus.OPEN
                );

        if (existingIncident.isPresent()) {
            return existingIncident.get();
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

        incident.setResolvedStatusCode(
                monitor.getLatestStatusCode()
        );

        incident.setResolvedResponseTimeMs(
                monitor.getLatestResponseTimeMs()
        );

        return incidentRepository.save(incident);
    }
}
