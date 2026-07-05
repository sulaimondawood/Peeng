package com.dawood.peeng.incident.models;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.incident.enums.IncidentStatus;
import com.dawood.peeng.incident.enums.Severity;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Incident extends MetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime resolvedAt;

    private Long initialResponseTimeMs;

    private Long resolvedResponseTimeMs;

    private Long durationSeconds;

    @Column(length = 2000)
    private String latestErrorMessage;

    private Integer failureCount;

    private Integer recoveryCount;

    private Integer initialStatusCode;

    private Integer resolvedStatusCode;

    private boolean acknowledged;

    private LocalDateTime acknowledgedAt;

    private UUID acknowledgedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

//    Further updates
//    @Column(columnDefinition = "TEXT")
//    private String rootCauseStatement; // From UI Section 01
//
//    @Column(columnDefinition = "TEXT")
//    private String preventativeActions; // From UI Section 02
//
//    @ElementCollection
//    @CollectionTable(name = "incident_remediation_tasks", joinColumns = @JoinColumn(name = "incident_id"))
//    @Column(name = "task")
//    private List<String> remediationTasks; // From UI Section 03 (One per line)
//
//    @Builder.Default
//    private boolean rcaLocked = false; // "Drafting State" vs "Locked State" toggle
}
