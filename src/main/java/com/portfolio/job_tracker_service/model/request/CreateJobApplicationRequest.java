package com.portfolio.job_tracker_service.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateJobApplicationRequest(
        @NotBlank(message = "company is required")
        String company,
        @NotBlank(message = "jobTitle is required")
        String jobTitle,
        String source,
        @NotNull(message = "appliedDate is required")
        LocalDate appliedDate,
        @NotNull(message = "status is required")
        String status,
        @NotNull(message = "location is required")
        String location,

        String jobUrl,

        String notes
) {}
