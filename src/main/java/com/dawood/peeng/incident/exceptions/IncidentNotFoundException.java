package com.dawood.peeng.incident.exceptions;

import com.dawood.peeng.common.enums.ErrorCode;
import com.dawood.peeng.common.exceptions.PeengException;
import org.springframework.http.HttpStatus;

public class IncidentNotFoundException extends PeengException {
    public IncidentNotFoundException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }
}
