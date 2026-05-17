package com.portfolio.job_tracker_service.service;

import com.portfolio.job_tracker_service.model.ApplicationStatus;

import java.util.Map;
import java.util.Set;

public class StatusTransitionValidator {

    private static final Map<ApplicationStatus, Set<ApplicationStatus>> ALLOWED_TRANSITIONS =
            Map.of(ApplicationStatus.APPLIED,Set.of(ApplicationStatus.INTERVIEW,ApplicationStatus.REJECTED,ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.INTERVIEW, Set.of(ApplicationStatus.OFFER,ApplicationStatus.REJECTED,ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.OFFER,Set.of(ApplicationStatus.REJECTED,ApplicationStatus.WITHDRAWN),
                    ApplicationStatus.REJECTED, Set.of(),
                    ApplicationStatus.WITHDRAWN,Set.of());

    public static TransitionResult validate(ApplicationStatus from, ApplicationStatus to) {
        if (ALLOWED_TRANSITIONS.getOrDefault(from, Set.of()).contains(to))
            return new TransitionResult.Allowed();
        return new TransitionResult.Denied(
            "Transition from %s to %s is not allowed".formatted(from, to)
        );
    }

    public static boolean commentRequired(ApplicationStatus status) {
        return switch (status) {
            case REJECTED, WITHDRAWN -> true;
            default -> false;
        };
    }
}
