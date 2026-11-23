package com.ptit.training.common.exception;

import com.ptit.training.common.ApiError;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Ngoại lệ cơ sở cho các vi phạm quy tắc nghiệp vụ (Business Rules).
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final List<ApiError.FieldIssue> details;

    public BusinessException(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, null);
    }

    public BusinessException(HttpStatus status, String errorCode, String message, List<ApiError.FieldIssue> details) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.details = details;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public List<ApiError.FieldIssue> getDetails() {
        return details;
    }
}
