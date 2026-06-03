package com.portfolio.job_tracker_service.service.impl;

import com.portfolio.job_tracker_service.model.UserDetails;
import com.portfolio.job_tracker_service.repository.UserDetailsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDetailsRepository userRepository;

    public UserService(UserDetailsRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDetails findOrCreateUser(UserDetails userDetails) {
        logger.info("Fetching user from database: {}", userDetails.id());

        // 1. Returning Supabase user
        Optional<UserDetails> existing = userRepository.findByUserId(userDetails.id());
        if (existing.isPresent()) return existing.get();

        // 2. Existing Keycloak user logging in via Supabase for the first time — migrate UUID
        Optional<UserDetails> byEmail = userRepository.findByEmail(userDetails.email());
        if (byEmail.isPresent()) {
            logger.info("Migrating user {} from Keycloak UUID {} to Supabase UUID {}",
                    userDetails.email(), byEmail.get().id(), userDetails.id());
            userRepository.migrateUserId(byEmail.get().id(), userDetails.id());
            clearAllCache();
            return userRepository.findByUserId(userDetails.id()).orElseThrow();
        }

        // 3. Brand new user
        return userRepository.save(userDetails);
    }

    @CacheEvict(value = "users", allEntries = true)
    public void clearAllCache() {
        logger.info("🧹 Clearing all user cache");
    }
}