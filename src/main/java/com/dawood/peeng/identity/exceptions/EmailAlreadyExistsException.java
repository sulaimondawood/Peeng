package com.dawood.peeng.identity.exceptions;

import org.springframework.http.HttpStatus;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;

public class EmailAlreadyExistsException extends PeengException {

  public EmailAlreadyExistsException(String message) {
    super(message, HttpStatus.CONFLICT, ErrorCode.RESOURCE_ALREADY_EXISTS);
  }

}
