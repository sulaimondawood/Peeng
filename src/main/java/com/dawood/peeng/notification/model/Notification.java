package com.dawood.peeng.notification.model;

import com.dawood.peeng.common.models.MetaData;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.notification.enums.NotificationChannel;

import com.dawood.peeng.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications_channel")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Notification extends MetaData {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    private String destination;

    private boolean enabled;

    @ManyToOne
    private Incident incident;
}
