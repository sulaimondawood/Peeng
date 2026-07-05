package com.dawood.peeng.incident.dto.request;

import com.dawood.peeng.incident.validator.constraints.ValidDateFilter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.util.UUID;

@ValidDateFilter
public record IncidentFilterRequest(
        String status,
        UUID monitorId,
        String dateBucket,
        LocalDate date,
        LocalDate startDate,
        LocalDate endDate,
        @Min(0)
        Integer page,
        @Min(1)
        @Max(100)
        Integer size
) {

    public IncidentFilterRequest {
        if (page == null) page = 0;

        if (size == null) size = 25;

    }

}
