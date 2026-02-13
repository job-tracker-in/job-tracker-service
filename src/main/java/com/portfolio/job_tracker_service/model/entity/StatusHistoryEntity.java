package com.portfolio.job_tracker_service.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record StatusHistoryEntity(
        UUID id,
        UUID jobId,
        String oldStatus,
        String newStatus,
        LocalDateTime changedAt,
        String comments,
        LocalDateTime createdAt
) {}
