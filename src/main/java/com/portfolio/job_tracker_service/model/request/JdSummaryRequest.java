package com.portfolio.job_tracker_service.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JdSummaryRequest(
        @NotBlank @Size(max = 5000) String jobDescription
) {}
