package com.dawood.peeng.incident.exceptions;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;
import org.springframework.http.HttpStatus;

public class IncidentException extends PeengException {
    public IncidentException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }
}
