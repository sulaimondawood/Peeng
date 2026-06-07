package com.dawood.peeng.monitor.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.models.Monitor;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MonitorStateService {

  public void handleSuccess(Monitor monitor) {

    boolean needsRecovery = monitor.getStatus() == MonitorStatus.DOWN
        || monitor.getStatus() == MonitorStatus.PENDING;

    if (needsRecovery
        && monitor.getConsecutiveSuccesses() >= monitor.getRecoveryThreshold()) {

      monitor.setStatus(MonitorStatus.UP);
      monitor.setLastStatusChangeAt(LocalDateTime.now());
      monitor.setIncidentOpen(false);

    }

  }

  public void handleFailure(Monitor monitor) {

    if (monitor.getConsecutiveFailures() >= monitor.getFailureThreshold()) {

      if (monitor.getStatus() != MonitorStatus.DOWN) {

        monitor.setStatus(MonitorStatus.DOWN);
        monitor.setLastStatusChangeAt(
            LocalDateTime.now());
        monitor.setIncidentOpen(true);

      }

    }

  }

}
