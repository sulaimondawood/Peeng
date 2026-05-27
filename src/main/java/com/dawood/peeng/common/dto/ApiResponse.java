package com.dawood.peeng.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@JsonInclude(value = Include.NON_EMPTY)
public class ApiResponse<T> {

  private String message;

  private T data;

  private Meta meta;

  private boolean success;

  public static <T> ApiResponse<T> success(String message, T data) {

    return ApiResponse.<T>builder()
        .success(true)
        .data(data)
        .message(message)
        .build();
  }

  public static <T> ApiResponse<T> success(
      String message,
      T data,
      Meta meta) {

    return ApiResponse.<T>builder()
        .success(true)
        .message(message)
        .data(data)
        .meta(meta)
        .build();
  }

}
