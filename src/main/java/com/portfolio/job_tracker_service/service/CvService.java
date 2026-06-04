package com.portfolio.job_tracker_service.service;

import com.portfolio.job_tracker_service.model.response.CvResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

public interface CvService {
    CvResponse upload(MultipartFile file, UUID userId);
    Optional<CvResponse> get(UUID userId);
    void delete(UUID userId);
    Optional<String> getExtractedText(UUID userId);
}
