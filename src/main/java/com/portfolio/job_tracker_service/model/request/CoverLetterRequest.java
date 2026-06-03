package com.portfolio.job_tracker_service.model.request;

import jakarta.validation.constraints.NotBlank;

public record CoverLetterRequest(
        String jobDescription,
        String tone
) {}