package com.dawood.peeng.tenant.model;

import com.dawood.peeng.identity.models.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workspace_invite_tokens")
public class WorkspaceInviteToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Tenant tenant;

    @ManyToOne
    private User invitedBy;

    private LocalDateTime expiresAt;
    private LocalDateTime acceptedAt;
}
