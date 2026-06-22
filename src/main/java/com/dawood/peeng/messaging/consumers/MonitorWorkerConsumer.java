package com.dawood.peeng.messaging.consumers;

import java.util.UUID;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.monitor.exceptions.MonitorNotFoundException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
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
                    .orElseThrow(() -> new MonitorNotFoundException(
                            "Monitor not found: " + monitorId,
                            HttpStatus.NOT_FOUND,
                            ErrorCode.NOT_FOUND));

        } catch (MonitorNotFoundException e) {
            log.error("Background task skipped: {}", e.getMessage());
            return;
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
            log.warn("Ping failed for monitor {}: {}", scheduledMonitor.getName(), e.getMessage());
            monitorCheckService.processFailure(scheduledMonitor, start, response, e.getMessage());

        } catch (Exception e) {
            log.error("Unexpected runtime error processing monitor ID: {}", monitorId, e);
        }

    }

}
