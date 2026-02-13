package com.portfolio.job_tracker_service.exception;

import org.springframework.http.HttpStatus;
import java.util.Map;

public class JobApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final Map<String, Object> details;

    public JobApplicationException(ErrorCode errorCode,
                                   HttpStatus status,
                                   String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.details = null;
    }

    public JobApplicationException(ErrorCode errorCode,
                                   HttpStatus status,
                                   String message,
                                   Map<String, Object> details) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.details = details;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
