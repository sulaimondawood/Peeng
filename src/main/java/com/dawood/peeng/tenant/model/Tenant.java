package com.dawood.peeng.tenant.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.tenant.enums.TenantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "tenants")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Tenant extends MetaData {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String workspaceName;

  @Column(nullable = false, unique = true)
  private String slug;

  @ManyToOne(fetch = FetchType.LAZY)
  private User owner;

  @Column(columnDefinition = "TEXT")
  private String settings;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private TenantStatus status = TenantStatus.ACTIVE;;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "tenant")
  @Builder.Default
  private List<Membership> memberships = new ArrayList<>();

}
