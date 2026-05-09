package com.dawood.peeng.subscriptions.models;

import java.time.LocalDateTime;
import java.util.UUID;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.subscriptions.enums.SubscriptionPlan;
import com.dawood.peeng.subscriptions.enums.SubscriptionStatus;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@Builder
public class Subscription extends MetaData {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

  private String providerCustomerId;

  private String providerSubscriptionId;

  @Enumerated(EnumType.STRING)
  private SubscriptionPlan plan;

  @Enumerated(EnumType.STRING)
  private SubscriptionStatus status;

  private LocalDateTime trialStartsAt;

  private LocalDateTime trialEndsAt;

  private LocalDateTime currentPeriodStart;

  private LocalDateTime currentPeriodEnd;

  @Column(nullable = false)
  @Builder.Default
  private boolean cancelAtPeriodEnd = false;

  private LocalDateTime endedAt;

  private LocalDateTime lastPaymentFailureAt;

}
