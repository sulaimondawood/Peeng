package com.dawood.peeng.incident.events;

import java.util.UUID;

public class IncidentResolvedEvent {

    private final UUID incidentId;

    public IncidentResolvedEvent(UUID incidentId) {

        if (incidentId == null) {
            throw new IllegalArgumentException("Incident ID cannot be null");
        }

        this.incidentId = incidentId;
    }

    public UUID getIncidentId() {
        return incidentId;
    }
}
