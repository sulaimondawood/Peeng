package com.dawood.peeng.monitor.exceptions;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;
import org.springframework.http.HttpStatus;

public class MonitorException extends PeengException {
    public MonitorException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }
}
