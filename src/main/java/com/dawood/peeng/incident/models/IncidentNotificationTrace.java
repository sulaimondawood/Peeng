package com.dawood.peeng.incident.models;

import com.dawood.peeng.incident.enums.DeliveryStatus;
import com.dawood.peeng.notification.enums.NotificationChannel;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "incident_notification_traces")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentNotificationTrace {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    private String targetChannelName;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

}
