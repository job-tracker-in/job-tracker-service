package com.portfolio.job_tracker_service.model.request;

import java.time.LocalDate;
import java.util.List;

public record JobAppFilterRequest(String status,
                                  String company,
                                  LocalDate from,
                                  LocalDate to,
                                  String sortOrder,
                                  Integer page,
                                  Integer size) {
}
