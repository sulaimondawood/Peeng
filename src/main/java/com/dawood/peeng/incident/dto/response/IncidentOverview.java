package com.dawood.peeng.incident.dto.response;

import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IncidentOverview {

   private LocalDateTime outageDuration;

   private MonitorResponseDTO monitor;

   private String assignedTo;

//   private

}
