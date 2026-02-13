package com.portfolio.job_tracker_service.service;

import com.portfolio.job_tracker_service.model.UserDetails;
import com.portfolio.job_tracker_service.model.request.CreateCompanyRequest;
import com.portfolio.job_tracker_service.model.request.CreateJobApplicationRequest;
import com.portfolio.job_tracker_service.model.request.JobAppFilterRequest;
import com.portfolio.job_tracker_service.model.request.UpdateStatusRequest;
import com.portfolio.job_tracker_service.model.response.CompanyResponse;
import com.portfolio.job_tracker_service.model.response.JobApplicationResponse;
import com.portfolio.job_tracker_service.model.response.PagedApplicationResponse;

import java.util.List;
import java.util.UUID;

public interface JobApplicationService {
    UUID createCompany(CreateCompanyRequest createCompanyRequest);
    CompanyResponse getCompanyByName(String name);

    List<CompanyResponse> getCompanies();

    UUID createJobApplication(CreateJobApplicationRequest applicationRequest, UUID userId);

    PagedApplicationResponse fetchApplications(UUID userId, JobAppFilterRequest jobAppFilterRequest);

    void deleteJobApplication(List<UUID> uuids);

    List<String> searchCompanyNames(String query, UUID userId);

    void updateStatusOrNotes(UUID applicationId, UpdateStatusRequest updateStatusRequest, UUID userId);

    JobApplicationResponse fetchApplicationHistory(UUID applicationId, UUID userId);
}
