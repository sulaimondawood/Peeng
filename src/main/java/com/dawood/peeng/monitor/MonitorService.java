package com.dawood.peeng.monitor;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.dawood.peeng.monitor.dtos.requests.CreateMonitorRequest;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.tenant.context.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

  private final MonitorRepository monitorRepository;

  public void createMonitor(CreateMonitorRequest payload) {

    UUID tenantId = TenantContext.getTenantId();

    // Monitor monitorConfig = Monitor.builder()
    // .

  }

}
