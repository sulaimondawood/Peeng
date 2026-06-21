package com.dawood.peeng.messaging.events;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentEvent {

    private String workspaceName;

    private String monitorName;

    private String monitorUrl;

    private String incidentId;

    private Integer statusCode;

    private Long responseTimeMS;

    private Integer failureCount;

    private String errorMessage;

    private Integer year;

    private String destination;

    private String startedAt;

    private String dashboardIncidentUrl;

    private Long durationSeconds;

    private String resolvedAt;


}
