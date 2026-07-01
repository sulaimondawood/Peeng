package com.dawood.peeng.monitor.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ResponseTimePoint {

    private LocalDateTime timestamp;

    private Long responseTimeMs;

}
