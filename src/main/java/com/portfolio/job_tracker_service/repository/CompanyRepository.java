package com.portfolio.job_tracker_service.repository;

import com.portfolio.job_tracker_service.model.entity.CompanyEntity;
import com.portfolio.job_tracker_service.model.request.CreateCompanyRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository {

    UUID createCompany(CreateCompanyRequest createCompanyRequest);
    Optional<CompanyEntity> getCompanyIdByName(String name);
    Optional<List<String>> searchCompanyNames(String name, UUID userId);
}
