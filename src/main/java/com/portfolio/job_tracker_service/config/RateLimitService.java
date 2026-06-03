package com.portfolio.job_tracker_service.config;

import com.portfolio.job_tracker_service.exception.ErrorCode;
import com.portfolio.job_tracker_service.exception.JobApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS = 10;
    private static final Duration WINDOW = Duration.ofHours(1);

    public void checkCoverLetterLimit(UUID userId) {
        String key = "rate:cover-letter:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, WINDOW);
        }
        if (count > MAX_REQUESTS) {
            throw new JobApplicationException(
                    ErrorCode.RATE_LIMIT_EXCEEDED,
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Too many cover letter requests. Max " + MAX_REQUESTS + " per hour.",
                    Map.of("retryAfterSeconds", 3600)
            );
        }
    }
}
