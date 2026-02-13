package com.portfolio.job_tracker_service.exception;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse (
    String errorCode,
    String message,
    int status,
    Instant timestamp,
    Map<String, Object> details
) {}