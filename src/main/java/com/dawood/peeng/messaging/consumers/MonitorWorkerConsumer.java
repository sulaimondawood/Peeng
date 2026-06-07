package com.dawood.peeng.messaging.consumers;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorWorkerConsumer {

  private final MonitorRepository monitorRepository;

  @RabbitListener(queues = RabbitMQConfig.SCHEDULER_ROUTING_QUEUE)
  public void consumeScheduledMonitor(UUID monitorId) {

    try {

      Monitor scheduledMonitor = monitorRepository.findById(monitorId)
          .orElseThrow(() -> new IllegalArgumentException("Monitor not found: " + monitorId));

    } catch (IllegalArgumentException e) {
      log.error("Background task skipped: {}", e.getMessage());
    }

  }

}
