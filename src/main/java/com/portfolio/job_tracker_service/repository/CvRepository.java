package com.portfolio.job_tracker_service.repository;

import com.portfolio.job_tracker_service.model.entity.UserCvEntity;

import java.util.Optional;
import java.util.UUID;

public interface CvRepository {
    void save(UserCvEntity entity);
    Optional<UserCvEntity> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}
