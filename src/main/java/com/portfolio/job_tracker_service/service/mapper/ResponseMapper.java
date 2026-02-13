package com.portfolio.job_tracker_service.service.mapper;

import com.portfolio.job_tracker_service.model.entity.CompanyEntity;
import com.portfolio.job_tracker_service.model.entity.JobApplicationEntity;
import com.portfolio.job_tracker_service.model.response.ApplicationResponse;
import com.portfolio.job_tracker_service.model.response.CompanyResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseMapper {

    public static CompanyResponse companyResponseMapper (CompanyEntity entity){
        return new CompanyResponse(entity.id(),entity.name(),entity.location(), entity.createdAt());
    }
    public static List<ApplicationResponse> applicationResponseMapper (List<JobApplicationEntity> jobApplicationEntities){
        return jobApplicationEntities.stream().map(entity ->
                new ApplicationResponse(entity.id(), entity.company(), entity.location(), entity.title(), entity.source(),
                        entity.status(), entity.appliedDate(), entity.notes(),entity.updatedAt(),entity.jobUrl())).collect(Collectors.toList());
    }
}
