package com.dawood.peeng.incident.enums;

import java.util.List;

public enum IncidentStatus {
    OPEN("open"),
    RESOLVED("resolved");

    private final String value;

    private IncidentStatus(String value){
        this.value=value;
    }

    public String getValue(){
        return value;
    }


    public static IncidentStatus fromString(String value){
        if(value == null){
             throw new IllegalArgumentException("Incident status cannot be null");
        }

        String cleanedValue = value.trim().toLowerCase();
        List<IncidentStatus> VALUES = List.of(values());

       return VALUES.stream()
                .filter(status->status.value.equals(value))
                .findFirst()
                .orElseThrow(()->new IllegalArgumentException("Unsupported incident status "+value));

    }
}
