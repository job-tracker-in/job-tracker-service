package com.portfolio.job_tracker_service.repository;

import com.portfolio.job_tracker_service.model.response.JobApplicationResponse;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApplicationHistoryRepository {
     Optional<JobApplicationResponse> findById(UUID applicationId, UUID userId);

}
