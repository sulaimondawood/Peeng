package com.dawood.peeng.monitor.models;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "monitor_checks",indexes = {
        @Index(
                name = "idx_tenant_monitor_checked_at",
                columnList = "tenant_id, monitor_id, checked_at"
        )
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorCheck extends MetaData {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "monitor_id")
  private Monitor monitor;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id")
  private Tenant tenant;

  private boolean successful;

  private Integer statusCode;

  private Long responseTimeMs;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  private LocalDateTime checkedAt;
}