package com.dawood.peeng.common.exceptions;

import com.dawood.peeng.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class BadRequestException extends PeengException {

    public BadRequestException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }
}
