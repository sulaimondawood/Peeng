package com.dawood.peeng.incident.events;

import java.util.UUID;

public class IncidentOpenedEvent {
    private final UUID incidentId;

    public IncidentOpenedEvent(UUID incidentId) {

        if (incidentId == null) {
            throw new IllegalArgumentException("Incident ID cannot be null");
        }

        this.incidentId = incidentId;
    }

    public UUID getIncidentId() {
        return incidentId;
    }
}
