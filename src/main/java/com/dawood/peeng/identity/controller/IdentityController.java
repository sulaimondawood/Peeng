package com.dawood.peeng.identity.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dawood.peeng.common.dto.ApiResponse;
import com.dawood.peeng.identity.dtos.request.RegisterDTO;
import com.dawood.peeng.identity.dtos.response.RegisterResponseDTO;
import com.dawood.peeng.identity.service.IdentityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class IdentityController {

  private final IdentityService identityService;

  @PostMapping("/register")
  public ApiResponse<RegisterResponseDTO> register(@RequestBody @Valid RegisterDTO payload) {

    RegisterResponseDTO response = identityService.register(payload);

    return ApiResponse.success(response.getMessage(), response);
  }

}
