package com.portfolio.job_tracker_service.repository.impl;

import com.portfolio.job_tracker_service.exception.ErrorCode;
import com.portfolio.job_tracker_service.exception.JobApplicationException;
import com.portfolio.job_tracker_service.model.entity.CompanyEntity;
import com.portfolio.job_tracker_service.model.request.CreateCompanyRequest;
import com.portfolio.job_tracker_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
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
public class CompanyRepositoryImpl implements CompanyRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final static RowMapper<CompanyEntity> rowMapper = (rs, rowNum) -> new CompanyEntity(
            (UUID) rs.getObject("id"),
            rs.getString("name"),
            rs.getString("location"),
            rs.getTimestamp("created_at").toLocalDateTime());

    @Override
    public Optional<CompanyEntity> getCompanyIdByName(String name) {
        String sql = """
                    SELECT * FROM company WHERE name = :name LIMIT 1
                """;

        try {
            log.debug("Querying company by name='{}'", name);
            var entity = jdbcTemplate.queryForObject(
                    sql, Map.of("name", name), rowMapper
            );

            log.info("Company found name='{}', id={}", name, entity.id());
            return Optional.of(entity);

        } catch (EmptyResultDataAccessException ex) {
            log.info("Company not found name='{}'", name);
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<String>> searchCompanyNames(String name, UUID userId) {
        String sql = """
                   Select c.name FROM job_application ja
                     JOIN company c ON ja.company_id = c.id
                     WHERE ja.user_id = :userId and c.name ILIKE :name
                     ORDER BY c.name LIMIT 10
                """;
        try {
            Map<String, Object> params = Map.of("name", "%" + name + "%","userId",userId);
            var response = jdbcTemplate.queryForList(sql, params, String.class);
            return Optional.of(response);
        }catch (EmptyResultDataAccessException ex){
            log.info("No match found for the company ={}", name);
            return Optional.empty();
        }


    }


    @Override
    public UUID createCompany(CreateCompanyRequest createCompanyRequest) {
        String sql = """
                insert into company(id, name, location)  values (:id, :name, :location)
                """;
        UUID id = UUID.randomUUID();
        Map<String, Object> params = Map.of("id", id,
                "name", createCompanyRequest.name(),
                "location", createCompanyRequest.location());
        var row = jdbcTemplate.update(sql, params);

        if (row == 0) {
            log.error("Failed to insert company with id={}", id);
            throw new JobApplicationException(
                    ErrorCode.COMPANY_CREATION_FAILED,
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to create company: " + createCompanyRequest.name(),
                    Map.of("companyName", createCompanyRequest.name()));
        }

        log.info("Company created successfully with id={}", id);
        return id;
    }

}
