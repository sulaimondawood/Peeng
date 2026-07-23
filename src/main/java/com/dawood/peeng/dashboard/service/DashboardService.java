package com.dawood.peeng.dashboard.service;

import com.dawood.peeng.dashboard.dto.DashboardOverviewDTO;
import com.dawood.peeng.dashboard.dto.MonitorStatsProjection;
import com.dawood.peeng.dashboard.repository.DashboardRepository;
import com.dawood.peeng.incident.dto.response.IncidentActivityDTO;
import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.mapper.IncidentMapper;
import com.dawood.peeng.incident.repository.IncidentActivityRepository;
import com.dawood.peeng.incident.repository.IncidentRepository;
import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import com.dawood.peeng.monitor.mapper.MonitorMapper;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.repository.MonitorRepository;
import com.dawood.peeng.tenant.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final MonitorRepository monitorRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentActivityRepository activityRepository;


    public DashboardOverviewDTO getWorkspaceOverview() {
        UUID tenantId = TenantContext.getTenantId();

        MonitorStatsProjection stats = dashboardRepository.getMonitorStats(tenantId);

        return new DashboardOverviewDTO(
                stats.activeMonitors(),
                stats.totalMonitors(),
                stats.downMonitors(),
                stats.openedIncidents(),
                stats.averageLatency()
        );
    }

    public List<MonitorResponseDTO> getRecentMonitors() {
        UUID tenantId = TenantContext.getTenantId();
        return monitorRepository.findTop5ByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(MonitorMapper::toDTO)
                .toList();

    }

    public List<IncidentDTO> getRecentIncidents() {
        UUID tenantId = TenantContext.getTenantId();
        return incidentRepository.findTop3ByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream()
                .map(IncidentMapper::toDTO)
                .toList();
    }

    public List<IncidentActivityDTO> getRecentActivityLogs() {
        UUID tenantId = TenantContext.getTenantId();
        return activityRepository.findTop5ByTenantIdOrderByOccurredAtDesc(tenantId)
                .stream()
                .map(incidentActivity -> {
                    IncidentActivityDTO dto = new IncidentActivityDTO();
                    dto.setMessage(incidentActivity.getMessage());
                    dto.setType(incidentActivity.getType());
                    dto.setTitle(incidentActivity.getTitle());
                    dto.setOccurredAt(incidentActivity.getOccurredAt());

                    return  dto;
                })
                .toList();
    }


}
