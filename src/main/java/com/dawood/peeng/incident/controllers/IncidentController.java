package com.dawood.peeng.incident.controllers;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.common.dto.Meta;
import com.dawood.peeng.incident.dto.request.IncidentFilterRequest;
import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.mapper.IncidentMapper;
import com.dawood.peeng.incident.models.Incident;
import com.dawood.peeng.incident.service.IncidentService;
import com.dawood.peeng.monitor.dtos.responses.MonitorCheckDTO;
import com.dawood.peeng.monitor.mapper.MonitorCheckMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("/opened")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getActiveIncident(){

        return ResponseEntity.ok().body(ApiResponse.success("Opened incidents fetched successfully", incidentService.getActiveIncidents()));

    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getAllIncidentS(
            @Valid IncidentFilterRequest request
            ){

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

        return ResponseEntity.ok().body(ApiResponse.success("All incidents fetched successfully",incidents ));

    }

}
