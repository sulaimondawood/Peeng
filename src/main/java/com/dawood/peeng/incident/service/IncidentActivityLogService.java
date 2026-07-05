package com.dawood.peeng.incident.service;

import com.dawood.peeng.incident.enums.ActivityType;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.models.IncidentActivity;
import com.dawood.peeng.incident.repository.IncidentActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IncidentActivityLogService {

    private final IncidentActivityRepository activityRepository;

    public void logActivity(Incident incident, ActivityType type, String title, String message){

        IncidentActivity newIncidentActivity = new IncidentActivity();
        newIncidentActivity.setIncident(incident);
        newIncidentActivity.setType(type);
        newIncidentActivity.setTitle(title);
        newIncidentActivity.setMessage(message);
        newIncidentActivity.setOccurredAt(LocalDateTime.now());

        activityRepository.save(newIncidentActivity);

    }

}
