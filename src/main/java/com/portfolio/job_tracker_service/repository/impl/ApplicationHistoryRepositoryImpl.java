package com.portfolio.job_tracker_service.repository.impl;

import com.portfolio.job_tracker_service.model.entity.StatusHistoryEntity;
import com.portfolio.job_tracker_service.model.response.JobApplicationHistoryResponse;
import com.portfolio.job_tracker_service.model.response.JobApplicationResponse;
import com.portfolio.job_tracker_service.repository.ApplicationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ApplicationHistoryRepositoryImpl implements ApplicationHistoryRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final static RowMapper<JobApplicationResponse> rowMapper = (rs, rowNum) -> new JobApplicationResponse(
            (UUID) rs.getObject("id"),
            rs.getString("company"),
            rs.getString("position"),
            rs.getString("current_status"),
            rs.getTimestamp("created_date").toLocalDateTime(),
            List.of());


    public void insert(StatusHistoryEntity e) {
        String sql = """
        INSERT INTO job_application_history (id, application_id, old_status, new_status, notes, updated_date)
        VALUES (:id, :jobId, :oldStatus, :newStatus, :notes, :createdAt)
    """;

        jdbc.update(sql, Map.of(
                "id", UUID.randomUUID(),
                "jobId", e.jobId(),
                "oldStatus", e.oldStatus(),
                "newStatus", e.newStatus(),
                "notes", e.comments(),
                "updated_date", e.createdAt()
        ));
    }

    public Optional<JobApplicationResponse> findById(UUID applicationId, UUID userId) {

        try{
            log.info("Fetching job application id={} userId={}", applicationId, userId);
            String sql = """
                SELECT ja.id, c.name AS company, ja.title AS position,
                       ja.status AS current_status, ja.created_at AS created_date
                FROM job_application ja
                JOIN company c ON ja.company_id = c.id
                WHERE ja.id = :applicationId AND ja.user_id = :userId
                """;

            var params = Map.of(
                    "applicationId", applicationId,
                    "userId", userId
            );

            var jobApplicationResponse = jdbc.queryForObject(sql, params, rowMapper);
            if (jobApplicationResponse == null) {
                log.warn("No job application found for id={} and userId={}", applicationId, userId);
                return Optional.empty();
            }

            // fetch history
            List<JobApplicationHistoryResponse> history = findHistory(applicationId, userId);

            log.info("Found application id={} with {} history items",
                    applicationId, history.size());

            JobApplicationResponse response = new JobApplicationResponse(
                    jobApplicationResponse.applicationId(),
                    jobApplicationResponse.company(),
                    jobApplicationResponse.position(),
                    jobApplicationResponse.currentStatus(),
                    jobApplicationResponse.createdDate(),
                    history
            );
            return Optional.of(response);
        }catch (EmptyResultDataAccessException exception){
            log.info("No job application found for id={} and userId={}",applicationId, userId);
            return Optional.empty();
        }
    }

    private List<JobApplicationHistoryResponse> findHistory(UUID applicationId, UUID userId) {

        log.info("Fetching history for application id={} userId={}", applicationId, userId);

        String historySql = """
                SELECT id, old_status, new_status, notes, updated_date
                FROM job_application_history
                WHERE application_id = :applicationId AND user_id = :userId
                ORDER BY updated_date DESC
                """;

        var params = Map.of(
                "applicationId", applicationId,
                "userId", userId
        );

        List<JobApplicationHistoryResponse> history = jdbc.query(historySql, params, (rs, row) ->
                new JobApplicationHistoryResponse(
                        rs.getObject("id", UUID.class),
                        rs.getString("old_status"),
                        rs.getString("new_status"),
                        rs.getString("notes"),
                        rs.getTimestamp("updated_date").toLocalDateTime()
                )
        );

        log.info("History rows fetched = {}", history.size());

        return history;
    }
}