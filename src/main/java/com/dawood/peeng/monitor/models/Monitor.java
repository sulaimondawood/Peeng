package com.dawood.peeng.monitor.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpMethod;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.enums.MonitorType;
import com.dawood.peeng.tenant.model.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monitors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Monitor extends MetaData {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  // Audit
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  // Basic info
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String url;

  @Column(unique = true)
  private String slug;

  // HTTP monitor configuration
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MonitorType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MonitorStatus status;

  @Enumerated(EnumType.STRING)
  private HttpMethod method;

  // Scheduling
  @Column(nullable = false)
  private Integer intervalSeconds;

  @Column(nullable = false)
  private Integer timeoutSeconds;

  // Monitor behavior
  @Builder.Default
  private boolean active = true;

  @Builder.Default
  private Integer retryAttempts = 2;

  @Builder.Default
  private Integer failureThreshold = 3;

  @Builder.Default
  private Integer recoveryThreshold = 1;

  // Assertions
  private Integer expectedStatusCode;

  private String expectedKeyword;

  // Latest check state
  private Integer latestStatusCode;

  private Integer latestResponseTimeMs;

  @Column(columnDefinition = "TEXT")
  private String latestErrorMessage;

  // Timestamps
  private LocalDateTime lastCheckedAt;

  private LocalDateTime lastSuccessfulCheckAt;

  private LocalDateTime lastFailedCheckAt;

  private LocalDateTime lastStatusChangeAt;

  // Incident state
  @Builder.Default
  private boolean incidentOpen = false;

  private LocalDateTime deletedAt;

  private boolean deleted;

}
