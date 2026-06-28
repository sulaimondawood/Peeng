package com.dawood.peeng.identity.exceptions;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;
import org.springframework.http.HttpStatus;

public class UnauthorizedException extends PeengException {
    public UnauthorizedException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }
}
