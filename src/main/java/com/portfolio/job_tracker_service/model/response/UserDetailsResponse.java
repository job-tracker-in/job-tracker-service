package com.portfolio.job_tracker_service.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDetailsResponse(UUID uuid, String firstName, String lastName,
                                  String email, String role)
{}
