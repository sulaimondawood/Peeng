package com.dawood.peeng.incident.controllers;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.common.dto.Meta;
import com.dawood.peeng.incident.dto.request.IncidentAssignmentRequest;
import com.dawood.peeng.incident.dto.request.IncidentFilterRequest;
import com.dawood.peeng.incident.dto.response.IncidentActivityDTO;
import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.dto.response.IncidentDiagnosticTraceDTO;
import com.dawood.peeng.incident.dto.response.IncidentOverview;
import com.dawood.peeng.incident.mapper.IncidentMapper;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.models.IncidentDiagnosticTrace;
import com.dawood.peeng.incident.service.IncidentService;
import com.dawood.peeng.membership.dtos.responses.MembershipResponseDTO;
import com.dawood.peeng.membership.service.MembershipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;
private final MembershipService membershipService;

    @GetMapping("/opened")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getActiveIncident() {

        return ResponseEntity.ok().body(ApiResponse.success("Opened incidents fetched successfully", incidentService.getActiveIncidents()));

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getAllIncidentS(
            @Valid IncidentFilterRequest request
    ) {

        Page<Incident> pagedIncident = incidentService.getAllIncidents(request);

        Meta meta = new Meta();
        meta.setPageNumber(pagedIncident.getNumber());
        meta.setPageSize(pagedIncident.getSize());
        meta.setTotalElements(pagedIncident.getTotalElements());
        meta.setTotalPages(pagedIncident.getTotalPages());
        meta.setLast(pagedIncident.isLast());

        List<IncidentDTO> incidents = pagedIncident.getContent().stream()
                .map(IncidentMapper::toDTO)
                .toList();

        return ResponseEntity.ok().body(ApiResponse.success("All incidents fetched successfully", incidents, meta));

    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<ApiResponse<IncidentOverview>> getIncidentOverview(@PathVariable("incidentId") UUID incidentId) {
        return ResponseEntity.ok()
                .body(ApiResponse.success("Request successful", incidentService.getIncidentDetails(incidentId)));

    }

    @PostMapping("/{incidentId}/trace")
    public ResponseEntity<ApiResponse<IncidentDiagnosticTraceDTO>> traceManualDiagnostic(@PathVariable("incidentId") UUID incidentId) {
        return ResponseEntity.ok()
                .body(ApiResponse.success("Request successful", incidentService.executeManualManualHandshake(incidentId)));

    }

    @PostMapping("/{incidentId}/assign")
    public ResponseEntity<ApiResponse<Void>> assignToMember(
            @PathVariable("incidentId") UUID incidentId,
            @RequestBody @Valid IncidentAssignmentRequest request) {

        incidentService.assignTeamMemberToIncident(incidentId, request.memberId());
        return ResponseEntity.ok()
                .body(ApiResponse.success("Request successful", null));

    }

    @GetMapping("/workspace/members")
    public ResponseEntity<ApiResponse<List<MembershipResponseDTO>>> getTeamMembers() {
        return ResponseEntity.ok()
                .body(ApiResponse.success("Request successful", membershipService.getAllMembersByTenant()));

    }


    @GetMapping("/{incidentId}/activity-timeline")
    public ResponseEntity<ApiResponse<List<IncidentActivityDTO>>> getMonitorIncidentActivityTimeline(@PathVariable("incidentId") UUID incidentId) {
        return ResponseEntity.ok()
                .body(ApiResponse.success("Request successful", incidentService.getMonitorIncidentActivityTimeline(incidentId)));

    }



}
