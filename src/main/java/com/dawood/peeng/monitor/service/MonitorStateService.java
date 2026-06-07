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

    if (monitor.getStatus().equals(MonitorStatus.DOWN)
        && monitor.getConsecutiveSuccesses() >= monitor.getRecoveryThreshold()) {

      monitor.setStatus(MonitorStatus.UP);
      monitor.setLastStatusChangeAt(LocalDateTime.now());

    }

    if (monitor.getStatus() == MonitorStatus.PENDING) {

      monitor.setStatus(MonitorStatus.UP);
      monitor.setLastStatusChangeAt(
          LocalDateTime.now());
    }

  }

}
