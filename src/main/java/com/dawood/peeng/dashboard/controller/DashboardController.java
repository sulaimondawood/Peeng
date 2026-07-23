package com.dawood.peeng.dashboard.controller;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.dashboard.dto.DashboardOverviewDTO;
import com.dawood.peeng.dashboard.service.DashboardService;
import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewDTO>> getOverview() {
        DashboardOverviewDTO overview = dashboardService.getWorkspaceOverview();
        return ResponseEntity.ok(ApiResponse.success("Dashboard overview retrieved successfully", overview));
    }

    @GetMapping("/monitors/recent")
    public ResponseEntity<ApiResponse<List<MonitorResponseDTO>>> getRecentMonitors() {
        List<MonitorResponseDTO> response = dashboardService.getRecentMonitors();
        return ResponseEntity.ok(ApiResponse.success("Recent monitors retrieved successfully", response));
    }

    @GetMapping("/incidents/recent")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getRecentIncidents() {
        List<IncidentDTO> response = dashboardService.getRecentIncidents();
        return ResponseEntity.ok(ApiResponse.success("Recent incidents retrieved successfully", response));
    }

}
