package com.dawood.peeng.monitor.controller;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.common.dto.Meta;
import com.dawood.peeng.monitor.dtos.requests.CreateMonitorRequest;
import com.dawood.peeng.monitor.dtos.responses.MonitorResponseDTO;
import com.dawood.peeng.monitor.enums.MonitorStatus;
import com.dawood.peeng.monitor.mapper.MonitorMapper;
import com.dawood.peeng.monitor.models.Monitor;
import com.dawood.peeng.monitor.service.MonitorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/monitors")
public class MonitorController {

  private final MonitorService monitorService;

  @PostMapping
  public ApiResponse<Void> createMonitor(@Valid @RequestBody CreateMonitorRequest payload) {
    monitorService.createMonitor(payload);
    return ApiResponse.success("Monitor Created", null);

  }

  @GetMapping
  public ApiResponse<List<MonitorResponseDTO>> monitors(
          @RequestParam(value = "pageNo", required = false, defaultValue = "0") int pageNo,
          @RequestParam(value = "pageSize", required = false, defaultValue = "25") int pageSize,
          @RequestParam(value = "status", required = false) MonitorStatus status,
          @RequestParam(value = "keyword", required = false) String keyword
          ){

    Page<Monitor> pagedResult = monitorService.getAllMonitors(status, keyword, pageNo, pageSize);

    List<MonitorResponseDTO> content = pagedResult
            .getContent()
            .stream()
            .map(MonitorMapper::toDTO)
            .toList();

    Meta meta = new Meta();
    meta.setPageNumber(pagedResult.getNumber());
    meta.setPageSize(pagedResult.getSize());
    meta.setTotalElements(pagedResult.getTotalElements());
    meta.setTotalPages(pagedResult.getTotalPages());
    meta.setLast(pagedResult.isLast());

    return ApiResponse.success("Successfully fectched monitors",content,meta);

  }

}
