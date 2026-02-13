package com.portfolio.job_tracker_service.model.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record JobApplicationHistoryResponse(UUID id,
                                            String oldStatus,
                                            String newStatus,
                                            String notes,
                                            LocalDateTime updatedDate) {
}
