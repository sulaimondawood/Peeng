package com.dawood.peeng.incident.dto.response;

import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class IncidentOverview {

   private Long outageDuration;

   private MonitorResponseDTO monitor;

   private String assignedTo;

   private List<IncidentNotificationTraceDTO> notificationTrace;

}
