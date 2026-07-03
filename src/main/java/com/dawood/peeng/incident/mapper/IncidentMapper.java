package com.dawood.peeng.incident.mapper;

import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.models.Incident;

public class IncidentMapper {
    public static IncidentDTO toDTO(Incident incident){
       return IncidentDTO.builder()
                .id(incident.getId())
                .status(incident.getStatus())
                .latestErrorMessage(incident.getLatestErrorMessage())
                .initialStatusCode(incident.getInitialStatusCode())
                .resolvedStatusCode(incident.getResolvedStatusCode())
                .durationSeconds(incident.getDurationSeconds())
                .build();
    }
}
