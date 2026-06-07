package com.portfolio.job_tracker_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        return Map.of(
                "error", "forbidden",
                "message", "Access denied: insufficient permissions"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = "Invalid value for '" + ex.getName() + "'. Expected UUID.";

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                400,
                "Bad Request",
                message,
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse response = new ErrorResponse(
                ErrorCode.VALIDATION_FAILED.name(),
                message,
                400,
                Instant.now(),
                null
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                "NOT_FOUND", ex.getMessage(), 404, Instant.now(), null);
        return ResponseEntity.status(404).body(response);
    }

    @ExceptionHandler(JobApplicationException.class)
    public ResponseEntity<ErrorResponse> handleJobApplicationException(JobApplicationException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode().name(),
                ex.getMessage(),
                ex.getStatus().value(),
                Instant.now(),
                ex.getDetails()
        );
        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        ErrorResponse response = new ErrorResponse(
                ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected error occurred",
                500,
                Instant.now(),
                null
        );
        return ResponseEntity.status(500).body(response);
    }
}
