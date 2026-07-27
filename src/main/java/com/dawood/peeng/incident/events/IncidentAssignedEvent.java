package com.dawood.peeng.incident.events;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class IncidentAssignedEvent {

    private UUID incidentAssignedId;

    private UUID monitorId;

    private String assigner;

    private String assignee;

    private UUID tenantId;

    private String email;

    public IncidentAssignedEvent(UUID incidentAssignedId, UUID monitorId, UUID tenantId, String assigner, String assignee, String email){
        this.incidentAssignedId = incidentAssignedId;
        this.assigner=assigner;
        this.assignee=assignee;
        this.monitorId=monitorId;
        this.tenantId=tenantId;
        this.email=email;
    }

}
