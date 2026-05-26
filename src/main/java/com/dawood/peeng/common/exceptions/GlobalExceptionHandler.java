package com.dawood.peeng.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dawood.peeng.common.dto.ApiError;
import com.dawood.peeng.common.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // @ExceptionHandler(EmailNotVerifiedException.class)
  // public ResponseEntity<ApiError>
  // emailNotVerifiedExceptionHandler(EmailNotVerifiedException ex,
  // HttpServletRequest request) {

  // ApiError body = ApiError.builder()
  // .code(ex.getCode().name())
  // .status(ex.getStatus().value())
  // .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
  // .message(ex.getMessage())
  // .path(request.getRequestURI())
  // .build();

  // return ResponseEntity.badRequest().body(body);

  // }

  @ExceptionHandler(PeengException.class)
  public ResponseEntity<ApiError> peengExceptionHandler(PeengException ex, HttpServletRequest request) {

    HttpStatus status = ex.getStatus() != null ? HttpStatus.resolve(ex.getStatus().value()) : HttpStatus.BAD_REQUEST;
    if (status == null) {
      status = HttpStatus.BAD_REQUEST;
    }

    ApiError body = ApiError.builder()
        .code(ex.getCode().name())
        .status(status.value())
        // .error( HttpStatus.BAD_REQUEST.getReasonPhrase())
        .error(status.getReasonPhrase())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity.badRequest().body(body);

  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    ApiError error = ApiError.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
        .message(ex.getMessage())
        .code(ErrorCode.BAD_REQUEST.name())
        .path(request.getRequestURI())
        .build();

    return ResponseEntity
        .badRequest()
        .body(error);
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
