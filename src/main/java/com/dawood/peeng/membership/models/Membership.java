package com.dawood.peeng.membership.models;

import java.util.UUID;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.models.User;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "memberships", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "tenant_id" })
})
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Membership extends MetaData {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RoleType role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tenant_id", nullable = false)
  private Tenant tenant;

}
