package com.portfolio.job_tracker_service.service;

import com.portfolio.job_tracker_service.model.request.CoverLetterRequest;
import com.portfolio.job_tracker_service.model.response.CoverLetterResponse;

import java.util.UUID;

public interface CoverLetterService {
    CoverLetterResponse generate(UUID applicationId, UUID userId, CoverLetterRequest request);
}
