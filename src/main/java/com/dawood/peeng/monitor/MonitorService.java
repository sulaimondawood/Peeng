package com.dawood.peeng.monitor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.identity.exceptions.UserNotFoundException;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.monitor.dtos.requests.CreateMonitorRequest;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.tenant.context.TenantContext;
import com.dawood.peeng.tenant.exceptions.TenantException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import com.dawood.peeng.utils.SlugUtils;
import com.dawood.peeng.utils.TimeConverterUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

  private final MonitorRepository monitorRepository;
  private final TenantRepository tenantRepository;
  private final UserRepository userRepository;

  public void createMonitor(CreateMonitorRequest payload) {

    long intervalInSeconds = payload.getCalculatedIntervalSeconds();

    if (intervalInSeconds < 30) {
      throw new IllegalArgumentException("Invalid monitor interval. Monitor interval cannot be less than 30 seconds");
    }

    if (payload.getTimeoutSeconds() < 2) {
      throw new IllegalArgumentException("Monitor timeout cannot be less than 2 seconds");
    }

    if (payload.getTimeoutSeconds() >= intervalInSeconds) {
      throw new IllegalArgumentException("Monitor timeout must be shorter than the monitor interval");
    }

    UUID tenantId = TenantContext.getTenantId();

    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    User currentUser = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new UserNotFoundException("User not found", HttpStatus.NOT_FOUND, ErrorCode.USER_NOT_FOUND));

    Tenant currentTenant = tenantRepository.findById(tenantId)
        .orElseThrow(() -> new TenantException("Tenant does not exists", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

    Monitor monitorConfig = Monitor.builder()
        .tenant(currentTenant)
        .createdBy(currentUser)
        .name(payload.getName())
        .url(payload.getUrl())
        .slug(SlugUtils.makeUniqueSlug(payload.getName()))
        .type(payload.getMonitorType())
        .status(MonitorStatus.PENDING)
        .method(payload.getMethod())
        .intervalSeconds(intervalInSeconds)
        .timeoutSeconds(payload.getTimeoutSeconds())
        .failureThreshold(payload.getFailureThreshold())
        .recoveryThreshold(payload.getRecoveryThreshold())
        .expectedStatusCode(payload.getExpectedStatusCode())
        .expectedKeyword(payload.getExpectedKeyword())
        .build();

    monitorRepository.save(monitorConfig);

  }

}
