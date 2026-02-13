package com.portfolio.job_tracker_service.repository;

import com.portfolio.job_tracker_service.model.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserDetailsRepository {

    boolean existsByUserId(String userId);
    Optional<UserDetails> findByUserId(UUID userId);
    UserDetails save(UserDetails userDetails);
}
