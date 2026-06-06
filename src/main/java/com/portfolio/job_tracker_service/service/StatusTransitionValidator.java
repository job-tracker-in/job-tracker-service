package com.portfolio.job_tracker_service.service;

import com.portfolio.job_tracker_service.model.ApplicationStatus;

public class StatusTransitionValidator {

    public static TransitionResult validate(ApplicationStatus from, ApplicationStatus to) {
        return new TransitionResult.Allowed();
    }

    public static boolean commentRequired(ApplicationStatus status) {
        return false;
    }
}
