package com.ptit.training.common.exception;

import org.springframework.http.HttpStatus;

public class DataScopeAccessDeniedException extends BusinessException {
    public DataScopeAccessDeniedException(String errorCode, String message) {
        super(HttpStatus.FORBIDDEN, errorCode, message);
    }
}
