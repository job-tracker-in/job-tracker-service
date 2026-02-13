package com.portfolio.job_tracker_service.model.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record JobApplicationResponse(UUID applicationId,
                                     String company,
                                     String position,
                                     String currentStatus,
                                     LocalDateTime createdDate,
                                     List<JobApplicationHistoryResponse> history) {
}
