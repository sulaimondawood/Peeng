package com.dawood.peeng.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dawood.peeng.common.dto.ApiError;
import com.dawood.peeng.common.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = PeengException.class)
  public ResponseEntity<ApiError> peengExceptionHandler(PeengException ex, HttpServletRequest request) {

    ApiError body = ApiError.builder()
        .code(ex.getCode().name())
        .status(ex.getStatus().value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.badRequest().body(body);

  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ApiError> handleRuntimeException(
      RuntimeException ex,
      HttpServletRequest request) {

    ApiError error = ApiError.builder()
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message(ex.getMessage())
        .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity
        .internalServerError()
        .body(error);
  }

}
