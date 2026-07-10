package com.dawood.peeng.incident.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.enums.ActivityType;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.models.IncidentActivity;
import com.dawood.peeng.incident.repository.IncidentActivityRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import com.dawood.peeng.tenant.exceptions.TenantException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IncidentActivityLogService {

    private final IncidentActivityRepository activityRepository;
    private final TenantRepository tenantRepository;

    public void logActivity(UUID tenantId, Incident incident, ActivityType type, String title, String message){

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(()->new TenantException("Tenant not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        IncidentActivity newIncidentActivity = new IncidentActivity();
        newIncidentActivity.setIncident(incident);
        newIncidentActivity.setType(type);
        newIncidentActivity.setTitle(title);
        newIncidentActivity.setMessage(message);
        newIncidentActivity.setTenant(tenant);
        newIncidentActivity.setOccurredAt(LocalDateTime.now());

        activityRepository.save(newIncidentActivity);

    }

}
