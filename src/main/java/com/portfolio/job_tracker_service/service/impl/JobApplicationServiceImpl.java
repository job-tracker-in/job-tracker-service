package com.portfolio.job_tracker_service.service.impl;

import com.portfolio.job_tracker_service.exception.ErrorCode;
import com.portfolio.job_tracker_service.exception.JobApplicationException;
import com.portfolio.job_tracker_service.model.UserDetails;
import com.portfolio.job_tracker_service.model.entity.JobApplicationEntity;
import com.portfolio.job_tracker_service.model.entity.CompanyEntity;
import com.portfolio.job_tracker_service.model.request.CreateCompanyRequest;
import com.portfolio.job_tracker_service.model.request.CreateJobApplicationRequest;
import com.portfolio.job_tracker_service.model.request.JobAppFilterRequest;
import com.portfolio.job_tracker_service.model.request.UpdateStatusRequest;
import com.portfolio.job_tracker_service.model.response.*;
import com.portfolio.job_tracker_service.repository.ApplicationHistoryRepository;
import com.portfolio.job_tracker_service.repository.CompanyRepository;
import com.portfolio.job_tracker_service.repository.JobApplicationRepository;
import com.portfolio.job_tracker_service.repository.UserDetailsRepository;
import com.portfolio.job_tracker_service.service.JobApplicationService;
import com.portfolio.job_tracker_service.service.mapper.ResponseMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationServiceImpl implements JobApplicationService {
    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ApplicationHistoryRepository applicationHistoryRepository;
    private final UserDetailsRepository userDetailsRepository;
    @Override
    public UUID createCompany(CreateCompanyRequest createCompanyRequest) {
        return companyRepository.createCompany(createCompanyRequest);
    }

    @Override
    public CompanyResponse getCompanyByName(String name) {
        var entity = companyRepository.getCompanyIdByName(name)
                .orElseThrow(() ->
                        new JobApplicationException(
                                ErrorCode.COMPANY_NOT_FOUND,
                                HttpStatus.NOT_FOUND,
                                "Company '" + name + "' not found",
                                Map.of("name",name)
                        )
                );
        return ResponseMapper.companyResponseMapper(entity);
    }

    @Override
    public List<CompanyResponse> getCompanies() {
        return null;
    }

    @Override
    public UUID createJobApplication(CreateJobApplicationRequest applicationRequest, UUID userId) {
        UUID companyId = companyRepository.getCompanyIdByName(applicationRequest.company().toUpperCase()).
                map(CompanyEntity::id).orElseGet(() ->companyRepository.createCompany(
                        new CreateCompanyRequest(applicationRequest.company().toUpperCase(), applicationRequest.location())
                ));

        return jobApplicationRepository.insert(applicationRequest, companyId, userId);
    }

    @Override
    public PagedApplicationResponse fetchApplications(UUID userId, JobAppFilterRequest req) {

        long total = jobApplicationRepository.countJobApplications(req, userId);
        if (total == 0) {
            return new PagedApplicationResponse(List.of(), req.page(), req.size(), 0, 0);
        }
        int totalPages = (int) Math.ceil((double) total / req.size());
        List<JobApplicationEntity> data = jobApplicationRepository.fetchApplications(userId, req);

        if (data.isEmpty()) {
            return new PagedApplicationResponse(List.of(), req.page(), req.size(), total, totalPages);
        }
        List<ApplicationResponse> mapped = ResponseMapper.applicationResponseMapper(data);
        return new PagedApplicationResponse(mapped, req.page(), req.size(), total, totalPages);
    }


    @Override
    public void deleteJobApplication(List<UUID> uuids) {
        jobApplicationRepository.deleteByIds(uuids);
    }

    @Override
    public List<String> searchCompanyNames(String name, UUID userId) {
        return companyRepository.searchCompanyNames(name,userId).orElseThrow(() ->
                new JobApplicationException(
                        ErrorCode.COMPANY_NOT_FOUND,
                        HttpStatus.NOT_FOUND,
                        "Company '" + name + "' not found",
                        Map.of("name", name)
                )
        );
    }

    @Override
    public void updateStatusOrNotes(UUID applicationId, UpdateStatusRequest updateStatusRequest, UUID userId) {
        jobApplicationRepository.updateStatus(applicationId,updateStatusRequest,userId);
    }

    @Override
    public JobApplicationResponse fetchApplicationHistory(UUID applicationId, UUID userId) {
        return applicationHistoryRepository.findById(applicationId, userId)
                .orElseThrow(() -> new JobApplicationException(
                ErrorCode.JOB_HISTORY_NOT_FOUND,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to fetch Job history application: " +  applicationId,
                Map.of("applicationId",  applicationId)));
    }

}
