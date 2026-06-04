package com.portfolio.job_tracker_service.controller;

import com.portfolio.job_tracker_service.model.response.CvResponse;
import com.portfolio.job_tracker_service.service.CvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cv")
@RequiredArgsConstructor
public class CvController {

    private final CvService cvService;

    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('application.write')")
    public ResponseEntity<CvResponse> upload(@RequestParam("file") MultipartFile file,
                                              @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        return ResponseEntity.ok(cvService.upload(file, userId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('application.read')")
    public ResponseEntity<CvResponse> get(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        return cvService.get(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('application.write')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        cvService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
