package com.dawood.peeng.membership.exceptions;

import org.springframework.http.HttpStatus;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;

public class MembershipException extends PeengException {

  public MembershipException(String message, HttpStatus status, ErrorCode errorCode) {
    super(message, status, errorCode);

  }

}
