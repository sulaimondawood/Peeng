package com.dawood.peeng.identity.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.identity.enums.Status;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.tenant.model.Tenant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "users")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class User extends MetaData {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(unique = true)
  private String email;

  @Column(nullable = false)
  private boolean emailVerified = false;

  private String passwordHash;

  @Column(nullable = false)
  private String name;

  private String avatarUrl;

  private LocalDateTime lastLoginAt;

  private LocalDateTime deletedAt;

  private int failedLoginAttempts;

  @Enumerated(EnumType.STRING)
  @Builder.Default
  @Column(nullable = false)
  private Status status = Status.ACTIVE;

  @OneToMany(mappedBy = "user")
  private List<Membership> memberships;

  private UUID lastActiveTenantId;

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  private EmailVerificationToken token;

}
