package com.portfolio.job_tracker_service.repository.impl;

import com.portfolio.job_tracker_service.model.UserDetails;
import com.portfolio.job_tracker_service.repository.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Repository
@Slf4j
@RequiredArgsConstructor
public class UserDetailsRepositoryImpl implements UserDetailsRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * Check if user exists by userId
     */
    @Override
    public boolean existsByUserId(String userId) {
        String sql = """
            SELECT COUNT(*)
            FROM user_details
            WHERE user_id = :userId
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        Integer count = namedParameterJdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    /**
     * Find user by userId
     */
    @Override
    public Optional<UserDetails> findByUserId(UUID userId) {
        String sql = """
            SELECT id, email, first_name, last_name, roles, created_at
            FROM user_details WHERE id = :userId
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId);

        try {
            UserDetails user = namedParameterJdbcTemplate.queryForObject(sql, params, rowMapper);
            log.info("User is found");
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    @Override
    public UserDetails save(UserDetails userDetails) {
        String sql = """
            INSERT INTO user_details (id, email, first_name, last_name, roles, created_at)
            VALUES (:userId, :email, :firstName, :lastName, :roles, :createdAt)
        """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userDetails.id())
                .addValue("email",userDetails.email())
                .addValue("firstName", userDetails.firstName())
                .addValue("lastName", userDetails.lastName())
                .addValue("roles", userDetails.roles())
                .addValue("createdAt", LocalDateTime.now());

        namedParameterJdbcTemplate.update(sql, params);
        log.info(" User saved to database: id={}", userDetails.id());
        return userDetails;
    }

    /**
     * RowMapper for UserDetails
     */

    private final RowMapper<UserDetails> rowMapper = (rs, rowNum) -> new UserDetails(
            (UUID) rs.getObject("id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("roles")
    );
}
