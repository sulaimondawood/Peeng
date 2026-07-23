package com.dawood.peeng.common.exceptions;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.dawood.peeng.common.dto.ApiError;
import com.dawood.peeng.common.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> methodArgumentNotValidExceptionHandler(MethodArgumentTypeMismatchException ex,
                                                                           HttpServletRequest request) {
        log.warn("Type mismatch on parameter '{}' at path: {}", ex.getName(), request.getRequestURI());

        String parameterName = ex.getName();
        Object providedValue = ex.getValue();

        String customMessage = String.format("Invalid value '%s' for parameter '%s'.", providedValue, parameterName);

        ApiError error = ApiError.builder()
                .code(ErrorCode.BAD_REQUEST.name())
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message(customMessage)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(error);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex,
                                                                           HttpServletRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        ex.getFieldErrors().forEach(err -> {
            validationErrors.put(err.getField(), err.getDefaultMessage());
        });

        ApiError error = ApiError.builder()
                .code(ErrorCode.VALIDATION_ERROR.name())
                .status(ex.getStatusCode().value())
                .error(HttpStatus.BAD_REQUEST.name())
                .message("Validation failed for one or more fields")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(error);

    }


    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Malformed JSON request body or invalid data formats")
                .code(ErrorCode.BAD_REQUEST.name())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {

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

    @ExceptionHandler(PeengException.class)
    public ResponseEntity<ApiError> handlePeengException(PeengException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus() != null ? ex.getStatus() : HttpStatus.BAD_REQUEST;

        ApiError body = ApiError.builder()
                .code(ex.getCode() != null ? ex.getCode().name() : ErrorCode.BAD_REQUEST.name())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(status).body(body);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnhandledException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception occurred at path: {}", request.getRequestURI(), ex);

        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected internal server error occurred. Please try again later.")
                .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
