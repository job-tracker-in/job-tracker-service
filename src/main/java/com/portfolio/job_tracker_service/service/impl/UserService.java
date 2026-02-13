package com.portfolio.job_tracker_service.service.impl;

import com.portfolio.job_tracker_service.model.UserDetails;
import com.portfolio.job_tracker_service.repository.UserDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDetailsRepository userRepository;

    public UserService(UserDetailsRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "users", key = "#userDetails.id()")
    @Transactional
    public UserDetails findOrCreateUser(UserDetails userDetails) {
        logger.info("🔍 Fetching user from database: {}", userDetails.id());

        return userRepository.findByUserId(userDetails.id())
                .orElseGet(() -> userRepository.save(userDetails));
    }

    @CacheEvict(value = "users", allEntries = true)
    public void clearAllCache() {
        logger.info("🧹 Clearing all user cache");
    }
}