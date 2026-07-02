package com.dawood.peeng.monitor.scheduler;

import com.dawood.peeng.messaging.producers.MonitorWorkerProducer;
import com.dawood.peeng.monitor.enums.MonitorLifecycleStatus;
import com.dawood.peeng.monitor.events.MonitorTaskMessage;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final MonitorRepository monitorRepository;
    private final MonitorWorkerProducer monitorWorkerProducer;

    @Scheduled(fixedDelay = 1000)
    public void scheduleChecks() {

        log.info("Scheduler Running...");

        UUID tenantId = TenantContext.getTenantId();

        List<Monitor> dueMonitors = monitorRepository
                .findAllByLifecycleAndNextCheckAtLessThanEqual(MonitorLifecycleStatus.ACTIVE, LocalDateTime.now());

        dueMonitors.forEach((monitor) -> {
            MonitorTaskMessage message = new MonitorTaskMessage();
            message.setMonitorId(monitor.getId());
            message.setTenantId(tenantId);

            monitorWorkerProducer.sendScheduledMonitor(message);
        });

    }

}
