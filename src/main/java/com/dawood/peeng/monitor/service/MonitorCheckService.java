package com.dawood.peeng.monitor.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.models.MonitorCheck;
import com.dawood.peeng.monitor.repository.MonitorCheckRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MonitorCheckService {

  private final MonitorCheckRepository monitorCheckRepository;

  public void processSuccess(Monitor monitor, long responseTime, int statusCode) {

    MonitorCheck monitorCheck = MonitorCheck.builder()
        .monitor(monitor)
        .successful(true)
        .statusCode(statusCode)
        .responseTimeMs(responseTime)
        .errorMessage(null)
        .checkedAt(LocalDateTime.now())
        .build();

  }

}
