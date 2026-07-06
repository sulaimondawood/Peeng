package com.dawood.peeng.notification.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.exceptions.IncidentNotFoundException;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.notification.enums.NotificationChannel;
import com.dawood.peeng.notification.model.NotificationChannelConfig;
import com.dawood.peeng.notification.respository.NotificationChannelConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationChannelConfigRepository channelConfigRepository;
    private final EmailNotificationProvider emailNotificationProvider;
    private final IncidentRepository incidentRepository;

    public void notifyIncidentOpened(UUID incidentId) {

        Incident openedIncident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException("Incident not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        List<NotificationChannelConfig> configs = channelConfigRepository.findByTenant_IdAndEnabledTrue(openedIncident.getTenant().getId());

        if(configs.isEmpty()){
            log.info("No active notification channels configured for Tenant: {}", openedIncident.getTenant().getId());
            return;
        }

        for(NotificationChannelConfig cfg: configs){
         NotificationChannel channel= cfg.getChannel();
            switch (channel){
                case EMAIL -> emailNotificationProvider.sendDownAlert(openedIncident,cfg);
//                case SLACK ->
                default -> log.warn("Unsupported alert delivery channel channel type detected: {}", cfg.getChannel());
            }

        }

    }

    public void notifyIncidentResolved(UUID incidentId) {

        Incident resolvedIncident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new IncidentNotFoundException(
                        "Incident not found",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        List<NotificationChannelConfig> configs = channelConfigRepository.findByTenant_IdAndEnabledTrue(resolvedIncident.getTenant().getId());

        configs.forEach((config) -> {
            emailNotificationProvider.sendRecoveryAlert(resolvedIncident, config);
        });

    }

}
