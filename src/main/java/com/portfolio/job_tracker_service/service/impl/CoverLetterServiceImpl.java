package com.portfolio.job_tracker_service.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.job_tracker_service.model.request.CoverLetterRequest;
import com.portfolio.job_tracker_service.model.response.CoverLetterResponse;
import com.portfolio.job_tracker_service.model.response.JobApplicationResponse;
import com.portfolio.job_tracker_service.service.CoverLetterService;
import com.portfolio.job_tracker_service.service.JobApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.portfolio.job_tracker_service.exception.ErrorCode;
import com.portfolio.job_tracker_service.exception.JobApplicationException;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CoverLetterServiceImpl implements CoverLetterService {

    private final RestClient geminiRestClient;
    private final JobApplicationService jobApplicationService;
    private final String model;

    public CoverLetterServiceImpl(
            @Qualifier("geminiRestClient") RestClient geminiRestClient,
            JobApplicationService jobApplicationService,
            @Value("${gemini.api.model:gemini-2.0-flash}") String model) {
        this.geminiRestClient = geminiRestClient;
        this.jobApplicationService = jobApplicationService;
        this.model = model;
    }

    @Override
    public CoverLetterResponse generate(UUID applicationId, UUID userId, CoverLetterRequest request) {
        JobApplicationResponse application = jobApplicationService.fetchApplicationHistory(applicationId, userId);

        String prompt = buildPrompt(application, request);

        GroqRequest groqRequest = new GroqRequest(
                model,
                List.of(
                        new Message("system", "You are a professional cover letter writer. Write concise, compelling cover letters tailored to the job and candidate."),
                        new Message("user", prompt)
                ),
                0.7,
                1024
        );

        try {
            GroqResponse groqResponse = geminiRestClient.post()
                    .uri("/chat/completions")
                    .body(groqRequest)
                    .retrieve()
                    .body(GroqResponse.class);
            String coverLetter = groqResponse.choices().get(0).message().content();
            return new CoverLetterResponse(coverLetter);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new JobApplicationException(
                        ErrorCode.RATE_LIMIT_EXCEEDED,
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Gemini API quota exceeded. Please try again in a minute.",
                        java.util.Map.of());
            }
            log.error("Gemini API error {}: {}", e.getStatusCode(), e.getMessage());
            throw new JobApplicationException(
                    ErrorCode.INTERNAL_ERROR,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Cover letter generation failed. Please try again.",
                    java.util.Map.of());
        }
    }

    private String buildPrompt(JobApplicationResponse application, CoverLetterRequest request) {
        String tone = request.tone() != null ? request.tone() : "professional";
        StringBuilder sb = new StringBuilder();
        sb.append("Write a ").append(tone).append(" cover letter for the following:\n\n");
        sb.append("Job Title: ").append(application.position()).append("\n");
        sb.append("Company: ").append(application.company()).append("\n");
        if (request.jobDescription() != null && !request.jobDescription().isBlank()) {
            sb.append("Job Description:\n").append(request.jobDescription()).append("\n");
        }
        sb.append("\nWrite only the cover letter text, ready to send. Do not include subject lines or placeholders.");
        return sb.toString();
    }

    // --- Internal Groq API DTOs ---

    record GroqRequest(
            String model,
            List<Message> messages,
            double temperature,
            @JsonProperty("max_tokens") int maxTokens
    ) {}

    record Message(String role, String content) {}

    record GroqResponse(List<Choice> choices) {}

    record Choice(Message message) {}
}
