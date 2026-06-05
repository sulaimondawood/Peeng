package com.dawood.peeng.monitor.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SchedulerService {

  private final MonitorRepository monitorRepository;

  @Scheduled(fixedRate = 1000)
  public void scheduleChecks() {

    List<Monitor> dueMonitors = monitorRepository.findAllByActiveTrueAndNextCheckAtBefore(LocalDateTime.now());

  }

}
