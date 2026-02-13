package com.portfolio.job_tracker_service.model.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobApplicationEntity(
        UUID id,
        UUID companyId,
        UUID userId,
        String title,
        String source,
        String status,
        LocalDate appliedDate,
        String jobUrl,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String company,
        String location
) {}
