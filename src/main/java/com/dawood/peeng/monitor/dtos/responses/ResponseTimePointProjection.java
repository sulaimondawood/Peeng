package com.dawood.peeng.monitor.dtos.responses;

import java.time.LocalDateTime;


public interface ResponseTimePointProjection {

     LocalDateTime timestamp();

     Long responseTimeMs();

    Long minResponseTime();

    Long maxResponseTime();

     long successfulCount();

}
