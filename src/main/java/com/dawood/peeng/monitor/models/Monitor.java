package com.dawood.peeng.monitor.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.monitor.enums.MonitorHttpType;
import com.dawood.peeng.monitor.enums.MonitorLifecycleStatus;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.enums.MonitorType;
import com.dawood.peeng.tenant.model.Tenant;

import jakarta.persistence.*;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_user_id")
  private User createdBy;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String url;

  @Column(unique = true)
  private String slug;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MonitorType type;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MonitorStatus status;

  @Enumerated(EnumType.STRING)
  private MonitorHttpType method;

  // Scheduling
  @Column(nullable = false)
  private Long intervalInSeconds;

  @Column(nullable = false)
  private LocalDateTime nextCheckAt;

  @Column(nullable = false)
  private Long timeoutInSeconds;

  @Builder.Default
  private Integer retryAttempts = 2;

  @Builder.Default
  private Integer failureThreshold = 3; // 3 failures will result to site down

  @Builder.Default
  private Integer recoveryThreshold = 1; // 1 success means UP

  @Builder.Default
  @Column(nullable = false)
  private Integer consecutiveFailures = 0;

  @Builder.Default
  @Column(nullable = false)
  private Integer consecutiveSuccesses = 0;

  // Assertions
  private Integer expectedStatusCode;

  private String expectedKeyword;

  // Latest check state
  private Integer latestStatusCode;

  private Long latestResponseTimeMs;

  @Column(columnDefinition = "TEXT")
  private String latestErrorMessage;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  private MonitorLifecycleStatus lifecycle = MonitorLifecycleStatus.ACTIVE;

  // Timestamps & Audit
  private LocalDateTime lastCheckedAt;

  private LocalDateTime lastSuccessfulCheckAt;

  private LocalDateTime lastFailedCheckAt;

  private LocalDateTime lastStatusChangeAt;

  private LocalDateTime pausedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "paused_by")
  private User pausedBy;

  private LocalDateTime resumedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "resumed_by")
  private User resumedBy;

  private LocalDateTime deletedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "deleted_by")
  private User deletedBy;
  // Incident state
  @Builder.Default
  private boolean incidentOpen = false;

  @OneToMany(mappedBy = "monitor")
  List<Incident> incidents = new ArrayList<>();

}
