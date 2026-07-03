package com.dawood.peeng.monitor.mapper;

import com.dawood.peeng.monitor.dtos.responses.MonitorCheckDTO;
import com.dawood.peeng.monitor.models.MonitorCheck;

public class MonitorCheckMapper {

    public static MonitorCheckDTO toDTO(MonitorCheck monitorCheck){
        MonitorCheckDTO response = new MonitorCheckDTO();
        response.setStatusCode(monitorCheck.getStatusCode());
        response.setResponseTimeMs(monitorCheck.getResponseTimeMs());
        response.setErrorMessage(monitorCheck.getErrorMessage());
        response.setCheckedAt(monitorCheck.getCheckedAt());
        response.setSuccessful(monitorCheck.isSuccessful());

        return response;
    }

}
