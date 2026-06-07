package com.dawood.peeng.monitor.service;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.models.MonitorCheck;
import com.dawood.peeng.monitor.repository.MonitorCheckRepository;
import com.dawood.peeng.monitor.repository.MonitorRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MonitorCheckService {

  private final MonitorCheckRepository monitorCheckRepository;
  private final MonitorRepository monitorRepository;
  private final MonitorStateService monitorStateService;

  @Transactional
  public void processSuccess(Monitor monitor, long responseTime, ResponseEntity<Void> response) {

    MonitorCheck monitorCheck = MonitorCheck.builder()
        .monitor(monitor)
        .successful(true)
        .statusCode(response.getStatusCode()
            .value())
        .responseTimeMs(responseTime)
        .errorMessage(null)
        .checkedAt(LocalDateTime.now())
        .build();

    monitorCheckRepository.save(monitorCheck);

    monitor.setNextCheckAt(
        monitor.getNextCheckAt().plusSeconds(monitor.getIntervalInSeconds()));
    monitor.setLatestStatusCode(statusCode);
    monitor.setLatestResponseTimeMs(responseTime);
    monitor.setLastCheckedAt(LocalDateTime.now());
    monitor.setLastSuccessfulCheckAt(LocalDateTime.now());

    monitor.setConsecutiveFailures(0);
    monitor.setConsecutiveSuccesses(
        monitor.getConsecutiveSuccesses() + 1);

    Monitor savedMonitor = monitorRepository.save(monitor);

    monitorStateService.handleSuccess(savedMonitor);

  }

}
