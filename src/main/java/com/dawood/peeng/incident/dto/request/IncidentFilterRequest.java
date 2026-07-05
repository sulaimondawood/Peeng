package com.dawood.peeng.incident.dto.request;

import com.dawood.peeng.incident.validator.constraints.ValidDateFilter;

import java.time.LocalDate;
import java.util.UUID;

@ValidDateFilter
public record IncidentFilterRequest(
        String statusStr,
        String dateBucket,
        LocalDate date,
        LocalDate startDate,
        LocalDate endDate,
        int page,
        int size
) {
}
