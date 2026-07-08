package com.dawood.peeng.incident.models;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.identity.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "incident_diagnostic_traces")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDiagnosticTrace extends MetaData {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    private User triggeredBy;

    private int statusCode;
    private long responseTimeMs;
    private boolean successful;


    @Column(columnDefinition = "TEXT")
    private String message;
}
