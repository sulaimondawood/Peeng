package com.dawood.peeng.monitor.exceptions;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;
import org.springframework.http.HttpStatus;

public class MonitorNotFoundException extends PeengException {

    public MonitorNotFoundException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }
}
