package com.dawood.peeng.incident.events;

import lombok.Getter;

import java.util.UUID;

@Getter
public class IncidentAssignedEvent {

    private final UUID incidentAssignedId;

    private final UUID monitorId;

    private final String assigner;

    private final String assignee;

    private final UUID tenantId;

    private final String email;

    public IncidentAssignedEvent(UUID incidentAssignedId, UUID monitorId, UUID tenantId, String assigner, String assignee, String email){
        this.incidentAssignedId = incidentAssignedId;
        this.assigner=assigner;
        this.assignee=assignee;
        this.monitorId=monitorId;
        this.tenantId=tenantId;
        this.email=email;
    }

}
