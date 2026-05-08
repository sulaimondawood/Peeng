package com.dawood.peeng.common.exceptions;

import org.springframework.http.HttpStatus;

import com.dawood.peeng.common.enums.ErrorCode;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PeengException extends RuntimeException {

  private final HttpStatus status;

  private final ErrorCode code;

  public PeengException(String message, HttpStatus status, ErrorCode errorCode) {
    super(message);
    this.status = status;
    this.code = errorCode;
  }

}
