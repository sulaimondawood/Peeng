package com.dawood.peeng.monitor.service;

import java.time.LocalDateTime;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

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
  public void processSuccess(Monitor monitor, long startTime, ResponseEntity<Void> response) {

    long responseTime = System.currentTimeMillis() - startTime;

    MonitorCheck monitorCheck = MonitorCheck.builder()
        .monitor(monitor)
        .successful(true)
        .statusCode(response.getStatusCode()
            .value())
        .responseTimeMs(responseTime)
        .checkedAt(LocalDateTime.now())
        .build();

    monitorCheckRepository.save(monitorCheck);

    monitor.setNextCheckAt(
        monitor.getNextCheckAt().plusSeconds(monitor.getIntervalInSeconds()));
    monitor.setConsecutiveFailures(0);
    monitor.setConsecutiveSuccesses(
        monitor.getConsecutiveSuccesses() + 1);
    monitor.setLatestStatusCode(response.getStatusCode().value());
    monitor.setLatestResponseTimeMs(responseTime);
    monitor.setLastCheckedAt(LocalDateTime.now());
    monitor.setLastSuccessfulCheckAt(LocalDateTime.now());

    Monitor savedMonitor = monitorRepository.save(monitor);

    monitorStateService.handleSuccess(savedMonitor);

  }

  @Transactional
  public void processFailure(Monitor monitor, long startTime, ResponseEntity<Void> response,
      String message) {

    long responseTime = System.currentTimeMillis() - startTime;

    MonitorCheck monitorCheck = MonitorCheck.builder()
        .monitor(monitor)
        .successful(false)
        .statusCode(response.getStatusCode()
            .value())
        .responseTimeMs(responseTime)
        .errorMessage(message)
        .checkedAt(LocalDateTime.now())
        .build();

    monitorCheckRepository.save(monitorCheck);

    monitor.setNextCheckAt(
        monitor.getNextCheckAt().plusSeconds(monitor.getIntervalInSeconds()));
    monitor.setConsecutiveFailures(monitor.getConsecutiveFailures() + 1);
    monitor.setConsecutiveSuccesses(
        0);
    monitor.setLatestStatusCode(response.getStatusCode().value());
    monitor.setLatestResponseTimeMs(responseTime);
    monitor.setLastCheckedAt(LocalDateTime.now());
    monitor.setLastSuccessfulCheckAt(LocalDateTime.now());

    Monitor savedMonitor = monitorRepository.save(monitor);

    monitorStateService.handleFailure(savedMonitor);

  }

}
