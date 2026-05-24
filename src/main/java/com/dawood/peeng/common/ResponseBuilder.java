package com.dawood.peeng.common;

import org.springframework.http.HttpStatus;

import com.dawood.peeng.common.dto.ApiError;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ResponseBuilder {

  public static ApiError buildError(HttpServletRequest req, HttpServletResponse res, String message, HttpStatus status,
      String error) {

    ApiError response = ApiError.builder()
        .status(status.value())
        .error(error)
        .message(message)
        .path(req.getRequestURI())
        .build();

    return response;

  }

}
