package com.portfolio.job_tracker_service.model.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationResponse(UUID id, String company, String location,
                                  String jobTitle, String source,
                                  String status, LocalDate appliedDate,
                                  String notes, LocalDateTime lastModifiedDate,
                                  String jobUrl) {
}
