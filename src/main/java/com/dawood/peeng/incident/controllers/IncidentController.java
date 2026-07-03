package com.dawood.peeng.incident.controllers;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.incident.dto.response.IncidentDTO;
import com.dawood.peeng.incident.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("/opened")
    public ResponseEntity<ApiResponse<List<IncidentDTO>>> getActiveIncident(){

        return ResponseEntity.ok().body(ApiResponse.success("Opened incidents fetched successfully", incidentService.getActiveIncidents()));

    }

}
