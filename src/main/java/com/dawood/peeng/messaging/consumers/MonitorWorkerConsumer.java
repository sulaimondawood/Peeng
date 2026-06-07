package com.dawood.peeng.messaging.consumers;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import com.dawood.peeng.monitor.service.MonitorCheckService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorWorkerConsumer {

  private final MonitorRepository monitorRepository;
  private final RestClient restClient;
  private final MonitorCheckService monitorCheckService;

  @RabbitListener(queues = RabbitMQConfig.SCHEDULER_ROUTING_QUEUE)
  public void consumeScheduledMonitor(UUID monitorId) {

    Monitor scheduledMonitor = null;

    try {

      scheduledMonitor = monitorRepository.findById(monitorId)
          .orElseThrow(() -> new IllegalArgumentException("Monitor not found: " + monitorId));

    } catch (IllegalArgumentException e) {
      log.error("Background task skipped: {}", e.getMessage());
    }

    long start = System.currentTimeMillis();

    ResponseEntity<Void> response = null;

    try {

      response = restClient.get()
          .uri(scheduledMonitor.getUrl())
          .retrieve()
          .toBodilessEntity();

      monitorCheckService.processSuccess(scheduledMonitor, start, response);
    } catch (RestClientException e) {
      long responseTime = System.currentTimeMillis() - start;
      log.warn("Ping failed for monitor {}: {}", scheduledMonitor.getName(), e.getMessage());

      monitorCheckService.processFailure(scheduledMonitor, responseTime, response, e);
    }

  }

}
