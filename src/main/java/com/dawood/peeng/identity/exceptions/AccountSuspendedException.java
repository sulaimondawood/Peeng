package com.dawood.peeng.identity.exceptions;

import org.springframework.http.HttpStatus;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;

public class AccountSuspendedException extends PeengException {

  public AccountSuspendedException(String message, HttpStatus status, ErrorCode errorCode) {
    super(message, status, errorCode);

  }

}
