package com.dawood.peeng.monitor.dtos.responses;

import java.time.LocalDateTime;


public interface ResponseTimePointProjection {

    LocalDateTime getTimestamp();

    Double getResponseTimeMs(); // Changed to Double because AVG() returns decimals

    Long getMinResponseTime();

    Long getMaxResponseTime();

    Long getSuccessfulCount();

}
