package com.dawood.peeng.monitor.service;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.identity.enums.RoleType;
import com.dawood.peeng.identity.exceptions.UnauthorizedException;
import com.dawood.peeng.identity.exceptions.UserNotFoundException;
import com.dawood.peeng.identity.models.User;
import com.dawood.peeng.identity.repository.UserRepository;
import com.dawood.peeng.identity.service.IdentityService;
import com.dawood.peeng.membership.exceptions.MembershipException;
import com.dawood.peeng.membership.models.Membership;
import com.dawood.peeng.membership.repository.MembershipRepository;
import com.dawood.peeng.monitor.dtos.requests.CreateMonitorRequest;
import com.dawood.peeng.monitor.enums.MonitorLifecycleStatus;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.exceptions.MonitorException;
import com.dawood.peeng.monitor.exceptions.MonitorNotFoundException;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import com.dawood.peeng.tenant.exceptions.TenantException;
import com.dawood.peeng.tenant.model.Tenant;
import com.dawood.peeng.tenant.repository.TenantRepository;
import com.dawood.peeng.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitorService {

    private final MonitorRepository monitorRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final IdentityService identityService;
    private final MembershipRepository membershipRepository;

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
                .intervalInSeconds(intervalInSeconds)
                .timeoutInSeconds(payload.getTimeoutSeconds())
                .nextCheckAt(LocalDateTime.now().plusSeconds(intervalInSeconds))
                .failureThreshold(payload.getFailureThreshold())
                .recoveryThreshold(payload.getRecoveryThreshold())
                .expectedStatusCode(payload.getExpectedStatusCode())
                .expectedKeyword(payload.getExpectedKeyword())
                .build();

        monitorRepository.save(monitorConfig);

    }

    public Page<Monitor> getAllMonitors(MonitorStatus statusStr, String keyword, int pageNo, int pageSize) {

        MonitorStatus status = null;

        if (statusStr != null && (statusStr.name().trim().equalsIgnoreCase("ALL"))) {
            status = statusStr;
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        UUID tenantId = TenantContext.getTenantId();

        return monitorRepository.findAllMonitors(tenantId, status, keyword, pageable);

    }

    public Monitor toggleMonitorState(UUID monitorId) {

        UUID tenantId = TenantContext.getTenantId();

        User currentUser = identityService.getCurrentLoggedInUser();

        Monitor existingMonitor = monitorRepository.findByIdAndTenantId(monitorId, tenantId)
                .orElseThrow(() -> new MonitorNotFoundException("Monitor not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        Membership membership = membershipRepository.findByUser_IdAndTenant_Id(currentUser.getId(), tenantId)
                .orElseThrow(() -> new MembershipException("User membership not found", HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND));

        if (membership.getRole() == RoleType.VIEWER) {
            throw new UnauthorizedException(
                    "You do not have permission to modify monitor states",
                    HttpStatus.FORBIDDEN,
                    ErrorCode.FORBIDDEN);
        }

        if (existingMonitor.getLifecycle() == MonitorLifecycleStatus.DELETED) {
            throw new MonitorException("You can not pause a deleted monitor", HttpStatus.BAD_REQUEST, ErrorCode.BAD_REQUEST);
        }

        if (existingMonitor.getLifecycle() == MonitorLifecycleStatus.ACTIVE) {
            existingMonitor.setLifecycle(MonitorLifecycleStatus.PAUSED);
            existingMonitor.setPausedAt(LocalDateTime.now());
            existingMonitor.setPausedBy(currentUser);
        } else if (existingMonitor.getLifecycle() == MonitorLifecycleStatus.PAUSED) {
            existingMonitor.setLifecycle(MonitorLifecycleStatus.ACTIVE);
            existingMonitor.setResumedAt(LocalDateTime.now());
            existingMonitor.setResumedBy(currentUser);
            existingMonitor.setNextCheckAt(LocalDateTime.now());
        }

       return monitorRepository.save(existingMonitor);

    }
}
