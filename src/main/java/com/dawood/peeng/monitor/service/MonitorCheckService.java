package com.dawood.peeng.monitor.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.incident.dto.response.CheckResult;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.models.MonitorCheck;
import com.dawood.peeng.monitor.repository.MonitorCheckRepository;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import com.dawood.peeng.tenant.exceptions.TenantException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MonitorCheckService {

    private static final Logger log = LoggerFactory.getLogger(MonitorCheckService.class);
    private final MonitorCheckRepository monitorCheckRepository;
    private final MonitorRepository monitorRepository;
    private final MonitorStateService monitorStateService;
    private final TenantRepository tenantRepository;

    @Transactional
    public void processSuccess(Monitor monitor, UUID tenantId, CheckResult result) {

        long responseTime = System.currentTimeMillis() - result.startTime();
        LocalDateTime now = LocalDateTime.now();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Tenant does not exists",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        int statusCode = result.response().getStatusCode().value();

        MonitorCheck monitorCheck = MonitorCheck.builder()
                .monitor(monitor)
                .tenant(tenant)
                .successful(true)
                .statusCode(statusCode)
                .responseTimeMs(responseTime)
                .checkedAt(LocalDateTime.now())
                .build();

        monitorCheckRepository.save(monitorCheck);

        if (result.isHighLatency()) {
            log.warn("Monitor {} responded with 200 OK but breached latency limits.", monitor.getName());
            this.processFailure(monitor, tenantId, result);
            return;
        }

        monitor.setNextCheckAt(
                now.plusSeconds(monitor.getIntervalInSeconds())
        );
        monitor.setConsecutiveFailures(0);
        monitor.setConsecutiveSuccesses(
                monitor.getConsecutiveSuccesses() + 1);
        monitor.setLatestStatusCode(statusCode);
        monitor.setLatestResponseTimeMs(responseTime);
        monitor.setLastCheckedAt(LocalDateTime.now());
        monitor.setLastSuccessfulCheckAt(LocalDateTime.now());

        Monitor savedMonitor = monitorRepository.save(monitor);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                monitorStateService.handleSuccess(savedMonitor);

            }
        });
    }

    @Transactional
    public void processFailure(
            Monitor monitor,
            UUID tenantId,
            CheckResult result
    ) {
        long responseTime = System.currentTimeMillis() - result.startTime();
        LocalDateTime now = LocalDateTime.now();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException(
                        "Tenant does not exists",
                        HttpStatus.NOT_FOUND,
                        ErrorCode.NOT_FOUND));

        Integer statusCode = Optional.ofNullable(result.response())
                .map((res) -> res.getStatusCode().value())
                .orElse(0);

        MonitorCheck monitorCheck = MonitorCheck.builder()
                .monitor(monitor)
                .tenant(tenant)
                .successful(result.isHighLatency() && result.message() == null)
                .statusCode(statusCode)
                .responseTimeMs(responseTime)
                .errorMessage(result.message())
                .checkedAt(LocalDateTime.now())
                .build();

        monitorCheckRepository.save(monitorCheck);

        monitor.setNextCheckAt(
                now.plusSeconds(monitor.getIntervalInSeconds()));
        monitor.setConsecutiveFailures(monitor.getConsecutiveFailures() + 1);
        monitor.setConsecutiveSuccesses(
                0);
        monitor.setLatestStatusCode(statusCode);
        monitor.setLatestResponseTimeMs(responseTime);
        monitor.setLastCheckedAt(LocalDateTime.now());
        monitor.setLatestErrorMessage(result.isHighLatency() && result.message() == null ?
                "High latency detected: Response took" + (result.responseTimeInMs() / 1000) + "secs" :
                result.message()
        );

        Monitor savedMonitor = monitorRepository.save(monitor);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                monitorStateService.handleFailure(savedMonitor, result);

            }
        });

    }


}
