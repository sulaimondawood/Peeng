package com.dawood.peeng.monitor.dtos.responses;

import java.time.LocalDateTime;

public interface UptimeBlockProjection {

    LocalDateTime getTimestamp();

    Double getResponseTimeMs();

    Long getSuccessfulCount();

    long getContainedCount();

    Double getUptimePercentage();
}
