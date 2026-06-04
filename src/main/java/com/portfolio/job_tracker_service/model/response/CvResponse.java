package com.portfolio.job_tracker_service.model.response;

import java.time.LocalDateTime;

public record CvResponse(String filename, LocalDateTime uploadedAt) {}
