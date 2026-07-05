package com.dawood.peeng.messaging.consumers;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.configs.RabbitMQConfig;
import com.dawood.peeng.incident.dto.response.CheckResult;
import com.dawood.peeng.monitor.events.MonitorTaskMessage;
import com.dawood.peeng.monitor.exceptions.MonitorNotFoundException;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import com.dawood.peeng.monitor.service.MonitorCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorWorkerConsumer {

    private final MonitorRepository monitorRepository;
    private final RestClient.Builder restClient;
    private final MonitorCheckService monitorCheckService;

    @RabbitListener(queues = RabbitMQConfig.SCHEDULER_ROUTING_QUEUE)
    public void consumeScheduledMonitor(MonitorTaskMessage message) {
        Monitor scheduledMonitor = null;

        UUID monitorId = message.getMonitorId();
        UUID tenantId = message.getTenantId();

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

        ResponseEntity<Void> response = null;
        boolean isTimeout = false;
        long responseTime = 0L;
        String errorMessage = null;
        long startTime = System.currentTimeMillis();

        try {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(Duration.ofSeconds(scheduledMonitor.getTimeoutInSeconds()));
            factory.setReadTimeout(Duration.ofSeconds(scheduledMonitor.getTimeoutInSeconds()));

            response = restClient.requestFactory(factory)
                    .build()
                    .get()
                    .uri(scheduledMonitor.getUrl())
                    .retrieve()
                    .toBodilessEntity();

        } catch (ResourceAccessException e) {
            log.warn("Connection or Read timed out for {}", scheduledMonitor.getName());
            isTimeout = true;
            errorMessage = e.getMessage();
        } catch (RestClientException e) {
            log.warn("Ping failed for monitor {}: {}", scheduledMonitor.getName(), e.getMessage());
            errorMessage = e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected runtime error processing monitor ID: {}", monitorId, e);
            throw e;
        }

        responseTime = System.currentTimeMillis() - startTime;

        long warningThreshold = (scheduledMonitor.getTimeoutInSeconds() * 1000) * 7 / 10;
        boolean isLatencyHigh = responseTime >= warningThreshold;

        CheckResult result = new CheckResult(startTime, response, errorMessage, isTimeout, isLatencyHigh, responseTime);

        if (errorMessage == null && !isTimeout && response != null) {
            monitorCheckService.processSuccess(scheduledMonitor, tenantId, result);
        } else {
            monitorCheckService.processFailure(scheduledMonitor, tenantId, result);
        }

    }

}
