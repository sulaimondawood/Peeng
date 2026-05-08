package com.dawood.peeng.common.exceptions;

import java.util.Map;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(value = PeengException.class)
  public Map<String, String> peengExceptionHandler(PeengException ex) {

    return null;

  }

}
