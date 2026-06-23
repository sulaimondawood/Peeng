package com.dawood.peeng.monitor.scheduler;

import com.dawood.peeng.messaging.producers.MonitorWorkerProducer;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

  private final MonitorRepository monitorRepository;
  private final MonitorWorkerProducer monitorWorkerProducer;

  @Scheduled(fixedDelay = 1000)
  public void scheduleChecks() {

    List<Monitor> dueMonitors = monitorRepository
        .findAllByActiveTrueAndNextCheckAtLessThanEqual(LocalDateTime.now());

    dueMonitors.forEach((monitor) -> {
      monitorWorkerProducer.sendScheduledMonitor(monitor.getId());
    });

  }

}
