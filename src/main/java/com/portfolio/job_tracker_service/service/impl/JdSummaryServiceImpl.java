package com.portfolio.job_tracker_service.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.portfolio.job_tracker_service.exception.ErrorCode;
import com.portfolio.job_tracker_service.exception.JobApplicationException;
import com.portfolio.job_tracker_service.model.request.JdSummaryRequest;
import com.portfolio.job_tracker_service.model.response.JdSummaryResponse;
import com.portfolio.job_tracker_service.service.JdSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JdSummaryServiceImpl implements JdSummaryService {

    private final RestClient groqRestClient;
    private final String model;

    public JdSummaryServiceImpl(
            @Qualifier("geminiRestClient") RestClient groqRestClient,
            @Value("${gemini.api.model:llama-3.3-70b-versatile}") String model) {
        this.groqRestClient = groqRestClient;
        this.model = model;
    }

    @Override
    public JdSummaryResponse summarise(JdSummaryRequest request) {
        String prompt = """
                Summarise this job description in exactly 3 short bullet points.
                Focus on: (1) main responsibilities, (2) key requirements, (3) what the company offers.
                Each bullet must be under 15 words.
                Return ONLY the 3 bullets, one per line, each starting with a dash (-).
                No intro, no headings, no extra text.

                Job Description:
                """ + request.jobDescription();

        GroqRequest groqRequest = new GroqRequest(
                model,
                List.of(
                        new Message("system", "You are a concise job description summariser. Return only bullet points."),
                        new Message("user", prompt)
                ),
                0.3,
                120
        );

        try {
            GroqResponse groqResponse = groqRestClient.post()
                    .uri("/chat/completions")
                    .body(groqRequest)
                    .retrieve()
                    .body(GroqResponse.class);

            String raw = groqResponse.choices().get(0).message().content();
            List<String> bullets = Arrays.stream(raw.split("\n"))
                    .map(String::trim)
                    .filter(line -> line.startsWith("-") || line.startsWith("•"))
                    .map(line -> line.replaceFirst("^[-•]\\s*", ""))
                    .filter(line -> !line.isBlank())
                    .limit(3)
                    .collect(Collectors.toList());

            if (bullets.isEmpty()) {
                // Fallback: split into up to 3 non-empty lines
                bullets = Arrays.stream(raw.split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isBlank())
                        .limit(3)
                        .collect(Collectors.toList());
            }

            return new JdSummaryResponse(bullets);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new JobApplicationException(
                        ErrorCode.RATE_LIMIT_EXCEEDED,
                        HttpStatus.TOO_MANY_REQUESTS,
                        "AI quota exceeded. Please try again in a moment.",
                        java.util.Map.of());
            }
            log.error("Groq API error {}: {}", e.getStatusCode(), e.getMessage());
            throw new JobApplicationException(
                    ErrorCode.INTERNAL_ERROR,
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Summary generation failed. Please try again.",
                    java.util.Map.of());
        }
    }

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
