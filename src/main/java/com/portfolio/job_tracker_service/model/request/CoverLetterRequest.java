package com.portfolio.job_tracker_service.model.request;

import jakarta.validation.constraints.NotBlank;

public record CoverLetterRequest(
        @NotBlank(message = "skills is required")
        String skills,
        String jobDescription,
        String tone
) {}