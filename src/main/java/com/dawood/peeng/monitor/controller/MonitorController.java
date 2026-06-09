package com.dawood.peeng.monitor.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.monitor.dtos.requests.CreateMonitorRequest;
import com.dawood.peeng.monitor.service.MonitorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

}
