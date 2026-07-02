package com.dawood.peeng.monitor.dtos.responses;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseTimeResponse {
//    private List<ResponseTimePoint> responseTimes;



    private Double averageResponseTime;

    private String range;

}
