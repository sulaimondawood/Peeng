package com.dawood.peeng.monitor.dtos.responses;

import com.dawood.peeng.monitor.enums.MonitorHttpType;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.enums.MonitorType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitorResponseDTO {
    private UUID id;

    private String name;

    private String url;

    private String slug;

    private MonitorType type;

    private MonitorStatus status;

    private MonitorHttpType method;

    private Long intervalInSeconds;

    private LocalDateTime nextCheckAt;

    private Long timeoutInSeconds;


}
