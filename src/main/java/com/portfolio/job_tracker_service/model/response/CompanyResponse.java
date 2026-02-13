package com.portfolio.job_tracker_service.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CompanyResponse (UUID uuid, String name,
                               String location, LocalDateTime createdAt)
{}
