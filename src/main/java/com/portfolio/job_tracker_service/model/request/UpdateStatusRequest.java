package com.portfolio.job_tracker_service.model.request;

public record UpdateStatusRequest(
        String status,
        String notes,
        java.time.LocalDate interviewDate,
        String salary,
        String recruiterName,
        String recruiterEmail
) {}
