package com.dawood.peeng.incident.dto.response;

import com.dawood.peeng.incident.enums.DeliveryStatus;
import com.dawood.peeng.notification.enums.NotificationChannel;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class IncidentNotificationTraceDTO {
    private UUID id;

    private NotificationChannel channel;

    private String targetChannelName;

    private DeliveryStatus status;

}
