package com.dawood.peeng.tenant.exceptions;

import org.springframework.http.HttpStatus;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;

public class WorkspaceSuspendedException extends PeengException {

  public WorkspaceSuspendedException(String message, HttpStatus status, ErrorCode errorCode) {
    super(message, status, errorCode);
    // TODO Auto-generated constructor stub
  }

}
