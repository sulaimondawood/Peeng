package com.dawood.peeng.notification.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.exceptions.IncidentNotFoundException;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.messaging.producers.EmailProducer;
import com.dawood.peeng.notification.model.NotificationChannelConfig;
import com.dawood.peeng.notification.respository.NotificationChannelConfigRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationChannelConfigRepository channelConfigRepository;
    private final EmailNotificationProvider emailNotificationProvider;
    private final EmailProducer emailProducer;
    private final IncidentRepository incidentRepository;

    public void notifyIncidentOpened(UUID incidentId) {

        Incident openedIncident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        List<NotificationChannelConfig> configs = channelConfigRepository.findByTenant_IdAndEnableTrue(openedIncident.getTenant().getId());

        configs.forEach((config) -> {
            emailNotificationProvider.sendDownAlert(openedIncident, config);
        });


    }

    public void notifyIncidentResolved(UUID incidentId) {

    }

}
