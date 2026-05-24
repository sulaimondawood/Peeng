package com.dawood.peeng.identity.exceptions;

import org.springframework.http.HttpStatus;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;

public class InvalidCredentialsException extends PeengException {

  public InvalidCredentialsException(String message, HttpStatus status, ErrorCode errorCode) {
    super(message, status, errorCode);
  }

}
