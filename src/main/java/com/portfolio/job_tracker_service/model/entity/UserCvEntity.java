package com.portfolio.job_tracker_service.model.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserCvEntity(
        UUID userId,
        String filename,
        byte[] pdfData,
        String extractedText,
        LocalDateTime uploadedAt
) {}
