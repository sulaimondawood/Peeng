package com.dawood.peeng.common.dto;

import java.time.Instant;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApiException {

  private int status;

  private String error;

  private String message;

  private String code;

  private String path;

  private Map<String, String> validationErrors;

  @Builder.Default
  private Instant timestamp = Instant.now();

}
