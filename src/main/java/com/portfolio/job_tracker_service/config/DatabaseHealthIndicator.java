package com.portfolio.job_tracker_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("db")
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        long start = System.currentTimeMillis();
        try{
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long latency = System.currentTimeMillis() - start;
            return Health.up().withDetail("Database","PostgreSQL")
                    .withDetail("Latency", latency)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
