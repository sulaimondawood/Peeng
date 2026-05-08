package com.dawood.peeng.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
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
