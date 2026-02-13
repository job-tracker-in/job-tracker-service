package com.portfolio.job_tracker_service.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyEntity(
        UUID id,
        String name,
        String location,
        LocalDateTime createdAt
) {}
