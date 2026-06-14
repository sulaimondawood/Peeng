package com.dawood.peeng.notification.model;

import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.notification.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class Notification {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    private String message;

    private String channel; // EMAIL, SMS, SLACK

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    private String recipient;

    @ManyToOne
    private Incident incident;
}
