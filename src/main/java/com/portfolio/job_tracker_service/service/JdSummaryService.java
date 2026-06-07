package com.portfolio.job_tracker_service.service;

import com.portfolio.job_tracker_service.model.request.JdSummaryRequest;
import com.portfolio.job_tracker_service.model.response.JdSummaryResponse;

public interface JdSummaryService {
    JdSummaryResponse summarise(JdSummaryRequest request);
}
