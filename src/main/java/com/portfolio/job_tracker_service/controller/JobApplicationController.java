package com.portfolio.job_tracker_service.controller;

import com.portfolio.job_tracker_service.model.request.CreateCompanyRequest;
import com.portfolio.job_tracker_service.model.request.CreateJobApplicationRequest;
import com.portfolio.job_tracker_service.model.request.JobAppFilterRequest;
import com.portfolio.job_tracker_service.model.request.UpdateStatusRequest;
import com.portfolio.job_tracker_service.model.response.CompanyResponse;
import com.portfolio.job_tracker_service.model.response.JobApplicationResponse;
import com.portfolio.job_tracker_service.model.response.PagedApplicationResponse;
import com.portfolio.job_tracker_service.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    @PostMapping("/company")
    @PreAuthorize("hasAuthority('company.manage')")
    public ResponseEntity<Void> createCompany(@RequestBody CreateCompanyRequest createCompanyRequest,
                                                UriComponentsBuilder uriBuilder){
        UUID companyId = jobApplicationService.createCompany(createCompanyRequest);
        URI location = uriBuilder
                .path("/company/{id}")
                .buildAndExpand(companyId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/company/{name}")
    @PreAuthorize("hasAuthority('company.manage')")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable  String name){
        var response = jobApplicationService.getCompanyByName(name);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/company")
    @PreAuthorize("hasAuthority('company.manage')")
    public ResponseEntity<List<CompanyResponse>> getCompanies(){
        var response = jobApplicationService.getCompanies();
        return ResponseEntity.ok().body(response);
    }
    @GetMapping("/companies")
    public ResponseEntity<List<String>> suggestCompanies(@RequestParam String name, @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        var response= jobApplicationService.searchCompanyNames(name,userId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/application")
    @PreAuthorize("hasAuthority('application.write')")
    public ResponseEntity<Void> createJobApplication(@RequestBody @Valid CreateJobApplicationRequest applicationRequest,
                                                     @AuthenticationPrincipal Jwt jwt,UriComponentsBuilder uriBuilder){
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        UUID id =jobApplicationService.createJobApplication(applicationRequest,userId);
        URI builder = uriBuilder
                .path("/application")
                .buildAndExpand(id)
                .toUri();
        return ResponseEntity.created(builder).build();
    }
    @GetMapping("/application")
    @PreAuthorize("hasAuthority('application.read')")
    public ResponseEntity<PagedApplicationResponse> getJobApplications(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        JobAppFilterRequest req = new JobAppFilterRequest(status, company, from, to, sortOrder, page, size);
        PagedApplicationResponse response = jobApplicationService.fetchApplications(userId, req);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/application/{id}")
    @PreAuthorize("hasAuthority('application.read')")
    public ResponseEntity<JobApplicationResponse> getJobApplications( @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        JobApplicationResponse jobApplicationResponse =jobApplicationService.fetchApplicationHistory(id, userId);
        return ResponseEntity.ok(jobApplicationResponse);
    }

    @DeleteMapping("/application")
    @PreAuthorize("hasAuthority('application.delete')")
    public ResponseEntity<Void> deleteJobApplication(@RequestBody List<UUID> uuids){
        jobApplicationService.deleteJobApplication(uuids);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/application/{id}")
    @PreAuthorize("hasAuthority('application.write')")
    public ResponseEntity<Void> updateStatusOrNotes(@PathVariable UUID id, @RequestBody UpdateStatusRequest updateStatusRequest,
                                                     @AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getClaim("sub"));
        jobApplicationService.updateStatusOrNotes(id,updateStatusRequest,userId);
        return ResponseEntity.accepted().build();
    }
 }
