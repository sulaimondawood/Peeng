package com.dawood.peeng.incident.listeners;

import com.dawood.peeng.incident.events.IncidentOpenedEvent;
import com.dawood.peeng.incident.events.IncidentResolvedEvent;
import com.dawood.peeng.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IncidentNotificationListener {

    private final NotificationService notificationService;

    @EventListener
    public void onIncidentOpened(IncidentOpenedEvent event) {
        notificationService.notifyIncidentOpened(event.getIncidentId());
    }

    @EventListener
    public void onIncidentResolved(IncidentResolvedEvent event) {
        notificationService.notifyIncidentResolved(event.getIncidentId());
    }

}
