package com.portfolio.job_tracker_service.controller;

import com.portfolio.job_tracker_service.config.RateLimitService;
import com.portfolio.job_tracker_service.model.request.CoverLetterRequest;
import com.portfolio.job_tracker_service.model.response.CoverLetterResponse;
import com.portfolio.job_tracker_service.service.CoverLetterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;
    private final RateLimitService rateLimitService;

    @PostMapping("/application/{id}/cover-letter")
    @PreAuthorize("hasAuthority('application.read')")
    public ResponseEntity<CoverLetterResponse> generateCoverLetter(
            @PathVariable UUID id,
            @RequestBody @Valid CoverLetterRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        rateLimitService.checkCoverLetterLimit(userId);
        CoverLetterResponse response = coverLetterService.generate(id, userId, request);
        return ResponseEntity.ok(response);
    }
}
