package com.dawood.peeng.monitor.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MonitorCheckDTO {
    private Integer statusCode;

    private Long responseTimeMs;

    private String errorMessage;

    private LocalDateTime checkedAt;

    private boolean successful;

}
