package com.portfolio.job_tracker_service.model.response;

import java.util.List;

public record PagedApplicationResponse (List<ApplicationResponse> data,
                                           int page,
                                           int size,
                                           long totalElements,
                                           int totalPages){
}
