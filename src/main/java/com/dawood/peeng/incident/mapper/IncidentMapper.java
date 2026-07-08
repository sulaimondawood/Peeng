package com.dawood.peeng.incident.mapper;

import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.dto.response.IncidentNotificationTraceDTO;
import com.dawood.peeng.incident.dto.response.IncidentOverview;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import com.dawood.peeng.monitor.models.Monitor;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class IncidentMapper {
    public static IncidentDTO toDTO(Incident incident) {
        MonitorResponseDTO monitorDTO = getMonitorResponseDTO(incident);

        return IncidentDTO.builder()
                .id(incident.getId())
                .status(incident.getStatus())
                .latestErrorMessage(incident.getLatestErrorMessage())
                .initialStatusCode(incident.getInitialStatusCode())
                .resolvedStatusCode(incident.getResolvedStatusCode())
                .durationSeconds(incident.getDurationSeconds())
                .monitor(monitorDTO)
                .build();
    }

    public static IncidentOverview toIncidentOverview(Incident incident) {
        MonitorResponseDTO monitorDTO = getMonitorResponseDTO(incident);

        List<IncidentNotificationTraceDTO> traces = Optional.ofNullable(incident.getNotificationTrace())
                .orElse(Collections.emptyList())
                .stream()
                .map(trace -> {
                    IncidentNotificationTraceDTO newTrace = new IncidentNotificationTraceDTO();
                    newTrace.setId(trace.getId());
                    newTrace.setStatus(trace.getStatus());
                    newTrace.setChannel(trace.getChannel());
                    newTrace.setTargetChannelName(trace.getTargetChannelName());
                    return newTrace;
                })
                .toList();


        IncidentOverview res = new IncidentOverview();
        res.setMonitor(monitorDTO);
        res.setOutageDuration(incident.getDurationSeconds());
        res.setAssignedTo(incident.getAcknowledgedBy() != null ? incident.getAcknowledgedBy().getName() : null);
        res.setNotificationTrace(traces);

        return res;

    }

    private static @NonNull MonitorResponseDTO getMonitorResponseDTO(Incident incident) {
        Monitor monitor = incident.getMonitor();

        MonitorResponseDTO monitorDTO = new MonitorResponseDTO();
        monitorDTO.setId(monitor.getId());
        monitorDTO.setName(monitor.getName());
        monitorDTO.setUrl(monitor.getUrl());
        monitorDTO.setStatus(monitor.getStatus());
        monitorDTO.setType(monitor.getType());
        monitorDTO.setIntervalInSeconds(monitor.getIntervalInSeconds());
        monitorDTO.setNextCheckAt(monitor.getNextCheckAt());
        monitorDTO.setTimeoutInSeconds(monitor.getTimeoutInSeconds());
        return monitorDTO;
    }
}
