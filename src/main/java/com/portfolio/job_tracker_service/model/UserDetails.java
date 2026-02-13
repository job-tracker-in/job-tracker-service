package com.portfolio.job_tracker_service.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetails(UUID id, String firstName, String lastName, String email, String roles){}
