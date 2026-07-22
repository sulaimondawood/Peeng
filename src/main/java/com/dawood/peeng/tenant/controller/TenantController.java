package com.dawood.peeng.tenant.controller;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.tenant.dtos.request.CreateTenantRequest;
import com.dawood.peeng.tenant.dtos.response.TenantSessionDTO;
import com.dawood.peeng.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/workspaces")
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    public ResponseEntity<ApiResponse<TenantSessionDTO>> createWorkspace(@Valid @RequestBody CreateTenantRequest request) {
        TenantSessionDTO response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Workspace created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantSessionDTO>>> getMyWorkspaces() {
        List<TenantSessionDTO> response = tenantService.listMyWorkspaces();
        return ResponseEntity.ok(ApiResponse.success("Workspaces retrieved successfully", response));
    }

    @PostMapping("/switch/{tenantId}")
    public ResponseEntity<ApiResponse<TenantSessionDTO>> changeActiveWorkspace(@PathVariable UUID tenantId) {
        TenantSessionDTO response = tenantService.switchWorkspace(tenantId);
        return ResponseEntity.ok(ApiResponse.success("Switched workspace successfully", response));
    }
}
