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

    public static boolean isValidTransitions(ApplicationStatus oldStatus, ApplicationStatus newStatus){
        return ALLOWED_TRANSITIONS.getOrDefault(oldStatus,Set.of()).contains(newStatus);
    }

    public static boolean commentRequired(ApplicationStatus status){
        return status == ApplicationStatus.REJECTED || status == ApplicationStatus.WITHDRAWN;
    }
}
