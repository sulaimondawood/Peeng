package com.dawood.peeng.incident.listeners;

import com.dawood.peeng.incident.events.IncidentOpenedEvent;
import com.dawood.peeng.incident.events.IncidentResolvedEvent;
import com.dawood.peeng.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIncidentOpened(IncidentOpenedEvent event) {
        log.info("Opened incident ID in Event Listener: {}", event.getIncidentId());
        notificationService.notifyIncidentOpened(event.getIncidentId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onIncidentResolved(IncidentResolvedEvent event) {
        notificationService.notifyIncidentResolved(event.getIncidentId());
    }

}
