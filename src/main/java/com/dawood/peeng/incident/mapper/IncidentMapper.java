package com.dawood.peeng.incident.mapper;

import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import com.dawood.peeng.monitor.models.Monitor;

public class IncidentMapper {
    public static IncidentDTO toDTO(Incident incident){

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
}
