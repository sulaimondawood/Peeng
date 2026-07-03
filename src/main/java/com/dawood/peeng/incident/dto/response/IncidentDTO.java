package com.dawood.peeng.incident.dto.response;

import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDTO {
    private UUID id;

    private IncidentStatus status;

    private String latestErrorMessage;

    private Integer initialStatusCode;

    private Integer resolvedStatusCode;

    private Long durationSeconds;

    private MonitorResponseDTO monitor;

}
