package com.portfolio.job_tracker_service.service;

public sealed interface TransitionResult permits TransitionResult.Allowed, TransitionResult.Denied {
    record Allowed() implements TransitionResult {}
    record Denied(String reason) implements TransitionResult {}
}
