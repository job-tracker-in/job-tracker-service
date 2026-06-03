package com.portfolio.job_tracker_service.repository.impl;

import com.portfolio.job_tracker_service.exception.ErrorCode;
import com.portfolio.job_tracker_service.exception.JobApplicationException;
import com.portfolio.job_tracker_service.model.ApplicationStatus;
import com.portfolio.job_tracker_service.model.entity.JobApplicationEntity;
import com.portfolio.job_tracker_service.model.request.CreateJobApplicationRequest;
import com.portfolio.job_tracker_service.model.request.JobAppFilterRequest;
import com.portfolio.job_tracker_service.model.request.UpdateStatusRequest;
import com.portfolio.job_tracker_service.repository.JobApplicationRepository;
import com.portfolio.job_tracker_service.service.StatusTransitionValidator;
import com.portfolio.job_tracker_service.service.TransitionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class JobApplicationRepositoryImpl implements JobApplicationRepository {
    private final NamedParameterJdbcTemplate jdbc;

    private final RowMapper<JobApplicationEntity> rowMapper = (rs, rowNum) -> new JobApplicationEntity(
            (UUID) rs.getObject("id"),
            (UUID) rs.getObject("company_id"),
            (UUID) rs.getObject("user_id"),
            rs.getString("title"),
            rs.getString("source"),
            rs.getString("status"),
            rs.getDate("applied_date") != null ? rs.getDate("applied_date").toLocalDate() : null,
            rs.getString("job_url"),
            rs.getString("notes"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime(),
            rs.getString("company"),
            rs.getString("location"),
            rs.getDate("interview_date") != null ? rs.getDate("interview_date").toLocalDate() : null,
            rs.getString("salary"),
            rs.getString("recruiter_name"),
            rs.getString("recruiter_email")
    );


    @Override
    public UUID insert(CreateJobApplicationRequest request, UUID companyId, UUID userId) {
        String sql = """
                    INSERT INTO job_application (id, company_id, user_id, title, source, status, applied_date, job_url, notes,
                                                interview_date, salary, recruiter_name, recruiter_email)
                    VALUES (:id, :company_id, :user_id, :title, :source, :status, :applied_date, :job_url, :notes,
                            :interview_date, :salary, :recruiter_name, :recruiter_email)
                """;

        UUID applicationId = UUID.randomUUID();
        Map<String, Object> params = new HashMap<>();
        params.put("id", applicationId);
        params.put("company_id", companyId);
        params.put("user_id", userId);
        params.put("title", request.jobTitle());
        params.put("source", request.source());
        params.put("status", request.status());
        params.put("applied_date", request.appliedDate());
        params.put("job_url", request.jobUrl());
        params.put("notes", request.notes());
        params.put("interview_date", request.interviewDate());
        params.put("salary", request.salary());
        params.put("recruiter_name", request.recruiterName());
        params.put("recruiter_email", request.recruiterEmail());
        var row = jdbc.update(sql, params);

        if (row == 0) {
            log.error("Failed to insert job application with id={}", applicationId);
            throw new JobApplicationException(
                    ErrorCode.COMPANY_CREATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create Job application: " + applicationId,
                    Map.of("applicationId", applicationId));
        }
        log.info("Job application created successfully with id={}", applicationId);
        return applicationId;
    }

    @Override
    public Optional<JobApplicationEntity> findById(UUID id) {
        String sql = """
                    SELECT * FROM job_application WHERE id = :id
                """;

        var result = jdbc.query(sql, Map.of("id", id), rowMapper);
        return result.stream().findFirst();
    }

    @Override
    public void updateStatus(UUID applicationId, UpdateStatusRequest updateStatusRequest, UUID userId) {
        // 1. First, fetch the current (old) status
        String fetchOldStatusSql = """
        SELECT status
        FROM job_application 
        WHERE id = :id AND user_id = :userId
        """;

        var fetchParams = Map.of(
                "id", applicationId,
                "userId", userId
        );
        String oldStatus;
        try {
            oldStatus = jdbc.queryForObject(fetchOldStatusSql, fetchParams, String.class);
        } catch (EmptyResultDataAccessException e) {
            log.error("Job application not found with id={} for userId={}", applicationId, userId);
            throw new JobApplicationException(
                    ErrorCode.JOB_NOT_FOUND,
                    HttpStatus.NOT_FOUND,
                    "Job application not found: " + applicationId,
                    Map.of("applicationId", applicationId));
        }

        // 2. Validate transition if status is changing
        String newStatusStr = updateStatusRequest.status();
        if (newStatusStr != null && !newStatusStr.equals(oldStatus)) {
            switch (StatusTransitionValidator.validate(
                    ApplicationStatus.valueOf(oldStatus),
                    ApplicationStatus.valueOf(newStatusStr))) {
                case TransitionResult.Allowed a -> {}
                case TransitionResult.Denied d -> throw new JobApplicationException(
                        ErrorCode.VALIDATION_FAILED,
                        HttpStatus.CONFLICT,
                        d.reason(),
                        Map.of("applicationId", applicationId, "from", oldStatus, "to", newStatusStr));
            }
        }

        // 3. Update the application
        String sql = """
        UPDATE job_application
        SET status = COALESCE(:status, status),
            notes = COALESCE(:notes, notes),
            interview_date = COALESCE(:interviewDate, interview_date),
            salary = COALESCE(:salary, salary),
            recruiter_name = COALESCE(:recruiterName, recruiter_name),
            recruiter_email = COALESCE(:recruiterEmail, recruiter_email),
            updated_at = :updatedAt
        WHERE id = :id AND user_id = :userId
        """;

        var params = new MapSqlParameterSource()
                .addValue("id", applicationId)
                .addValue("status", updateStatusRequest.status())
                .addValue("notes", updateStatusRequest.notes())
                .addValue("interviewDate", updateStatusRequest.interviewDate())
                .addValue("salary", updateStatusRequest.salary())
                .addValue("recruiterName", updateStatusRequest.recruiterName())
                .addValue("recruiterEmail", updateStatusRequest.recruiterEmail())
                .addValue("updatedAt", LocalDateTime.now())
                .addValue("userId", userId);

        int row = jdbc.update(sql, params);
        if (row == 0) {
            log.error("Failed to update job application with id={}", applicationId);
            throw new JobApplicationException(
                    ErrorCode.JOB_UPDATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to update job application: " + applicationId,
                    Map.of("applicationId", applicationId));
        }

        // 4. Insert history ONLY if status changed
        String newStatus = updateStatusRequest.status();
        if (newStatus != null && !newStatus.equals(oldStatus)) {
            String insertHistorySql = """
            INSERT INTO job_application_history 
            (application_id, user_id, old_status, new_status, notes, updated_date)
            VALUES (:applicationId, :userId, :oldStatus, :newStatus, :notes, :updatedDate)
            """;

            var historyParams = new MapSqlParameterSource()
                    .addValue("applicationId", applicationId)
                    .addValue("userId", userId)
                    .addValue("oldStatus", oldStatus)
                    .addValue("newStatus", newStatus)
                    .addValue("notes", updateStatusRequest.notes() != null ? updateStatusRequest.notes() : "Status updated")
                    .addValue("updatedDate", LocalDateTime.now());

            jdbc.update(insertHistorySql, historyParams);

            log.info("Status history recorded: {} -> {} for application {}", oldStatus, newStatus, applicationId);
        }

        log.info("Job application updated successfully with id={}", applicationId);

    }

    @Override
    public List<JobApplicationEntity> fetchApplications(UUID userId, JobAppFilterRequest req) {

        StringBuilder sql = new StringBuilder("""
        SELECT ja.*, c.name AS company, c.location as location
        FROM job_application ja
        JOIN company c ON ja.company_id = c.id
        WHERE ja.user_id = :userId
    """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        // filters
        if (req.status() != null) {
            sql.append(" AND ja.status = :status");
            params.put("status", req.status());
        }

        if (req.company() != null) {
            sql.append(" AND c.name = :company");
            params.put("company", req.company().toUpperCase());
        }

        if (req.from() != null) {
            sql.append(" AND ja.applied_date >= :fromDate");
            params.put("fromDate", req.from());
        }

        if (req.to() != null) {
            sql.append(" AND ja.applied_date <= :toDate");
            params.put("toDate", req.to());
        }

        // sorting (only applied_date)
        String direction = "DESC";
        if ("asc".equalsIgnoreCase(req.sortOrder())) {
            direction = "ASC";
        }

        sql.append(" ORDER BY ja.applied_date ").append(direction);

        // pagination
        sql.append(" LIMIT :limit OFFSET :offset");
        params.put("limit", req.size());
        params.put("offset", req.page() * req.size());

        return jdbc.query(sql.toString(), params, rowMapper);
    }


    @Override
    public long countJobApplications(JobAppFilterRequest req, UUID userId) {

        StringBuilder sql = new StringBuilder("""
        SELECT COUNT(*)
        FROM job_application ja
        JOIN company c ON ja.company_id = c.id
        WHERE ja.user_id = :userId
    """);

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        if (req.status() != null) {
            sql.append(" AND ja.status = :status");
            params.put("status", req.status());
        }

        if (req.company() != null) {
            sql.append(" AND c.name = :company");
            params.put("company", req.company());
        }

        if (req.from() != null) {
            sql.append(" AND ja.applied_date >= :fromDate");
            params.put("fromDate", req.from());
        }

        if (req.to() != null) {
            sql.append(" AND ja.applied_date <= :toDate");
            params.put("toDate", req.to());
        }

        Long count = jdbc.queryForObject(sql.toString(), params, Long.class);
        return count != null ? count :0L;
    }

}
