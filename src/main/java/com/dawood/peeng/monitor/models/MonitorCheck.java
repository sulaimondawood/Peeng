package com.dawood.peeng.monitor.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.dawood.peeng.common.models.MetaData;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "monitor_checks")
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

  private boolean successful;

  private Integer statusCode;

  private Long responseTimeMs;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;

  private LocalDateTime checkedAt;
}