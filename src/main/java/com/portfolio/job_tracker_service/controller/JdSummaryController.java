package com.portfolio.job_tracker_service.controller;

import com.portfolio.job_tracker_service.model.request.JdSummaryRequest;
import com.portfolio.job_tracker_service.model.response.JdSummaryResponse;
import com.portfolio.job_tracker_service.service.JdSummaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JdSummaryController {

    private final JdSummaryService jdSummaryService;

    @PostMapping("/jd/summarise")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JdSummaryResponse> summarise(
            @RequestBody @Valid JdSummaryRequest request) {
        return ResponseEntity.ok(jdSummaryService.summarise(request));
    }
}
