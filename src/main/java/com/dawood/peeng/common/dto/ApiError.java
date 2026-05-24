package com.dawood.peeng.common.dto;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonInclude(value = Include.NON_EMPTY)
public class ApiError {

  private int status;

  private String error;

  private String message;

  private String code;

  private String path;

  private Map<String, String> validationErrors;

  @Builder.Default
  private Instant timestamp = Instant.now();

}
