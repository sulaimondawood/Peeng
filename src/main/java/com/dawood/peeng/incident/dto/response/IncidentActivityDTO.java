package com.dawood.peeng.incident.dto.response;

import com.dawood.peeng.incident.enums.ActivityType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IncidentActivityDTO {

    private LocalDateTime occurredAt;

    private String title;

    private String message;

    private ActivityType type;

}
