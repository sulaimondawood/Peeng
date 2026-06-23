package com.dawood.peeng.monitor.mapper;

import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import com.dawood.peeng.monitor.models.Monitor;

public class MonitorMapper {
    public static MonitorResponseDTO toDTO(Monitor monitor){
        return  MonitorResponseDTO.builder()
                .id(monitor.getId())
                .name(monitor.getName())
                .url(monitor.getUrl())
                .slug(monitor.getSlug())
                .type(monitor.getType())
                .status(monitor.getStatus())
                .method(monitor.getMethod())
                .intervalInSeconds(monitor.getIntervalInSeconds())
                .nextCheckAt(monitor.getNextCheckAt())
                .timeoutInSeconds(monitor.getTimeoutInSeconds())
                .build();

    }

}
