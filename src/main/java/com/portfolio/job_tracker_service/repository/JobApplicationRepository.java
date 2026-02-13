package com.portfolio.job_tracker_service.repository;

import com.portfolio.job_tracker_service.model.entity.JobApplicationEntity;
import com.portfolio.job_tracker_service.model.request.CreateJobApplicationRequest;
import com.portfolio.job_tracker_service.model.request.JobAppFilterRequest;
import com.portfolio.job_tracker_service.model.request.UpdateStatusRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface JobApplicationRepository {
    UUID insert(CreateJobApplicationRequest request, UUID companyId, UUID userId);
    Optional<JobApplicationEntity> findById(UUID id);
    void updateStatus(UUID applicationId, UpdateStatusRequest updateStatusRequest, UUID userId);
    List<JobApplicationEntity> fetchApplications(UUID userId, JobAppFilterRequest jobAppFilterRequest);
    void deleteByIds(List<UUID> uuids);
    long countJobApplications(JobAppFilterRequest jobAppFilterRequest, UUID userId);
}
