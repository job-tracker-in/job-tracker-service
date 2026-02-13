package com.portfolio.job_tracker_service.config;

import com.portfolio.job_tracker_service.model.UserDetails;
import com.portfolio.job_tracker_service.service.JobApplicationService;
import com.portfolio.job_tracker_service.service.impl.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Filter to automatically create user entries in the database
 * when they first authenticate via Keycloak.
 * <p>
 * This ensures that the user_id from JWT token exists in the user_details table
 * before any business logic tries to use it (preventing foreign key violations).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserAutoCreateFilter extends OncePerRequestFilter {

    private final JobApplicationService userService1;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            // Get the authenticated user from Security Context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Only process if user is authenticated and principal is JWT
            if (authentication != null &&
                    authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof Jwt jwt) {

                // Extract user information from JWT claims
                String userId = jwt.getClaimAsString("sub");

                if (userId != null && !userId.isBlank()) {

                    String roles = "";
                    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
                    if (resourceAccess != null && resourceAccess.containsKey("jobtracker-api")) {
                        Map<String, Object> jobTracker = (Map<String, Object>) resourceAccess.get("jobtracker-api");
                        if (jobTracker != null && jobTracker.containsKey("roles")) {
                            List<String> rolesList = (List<String>) jobTracker.get("roles");
                            roles = String.join(",", rolesList);
                        }
                    }
                    String email = jwt.getClaimAsString("email");
                    String firstName = jwt.getClaimAsString("given_name");
                    String lastName = jwt.getClaimAsString("family_name");

                    UserDetails user = userService.findOrCreateUser(new UserDetails(UUID.fromString(userId),firstName,lastName,email,roles));

                    logger.debug("User loaded: " + user.email());
                    request.setAttribute("currentUser", user);

                    log.debug("User validation complete for userId: {}", userId);
                }
            }
        } catch (Exception e) {
            // Log error but don't break the request flow
            // This ensures the application continues to work even if user sync fails
            log.error("Error in UserAutoCreateFilter - continuing request processing", e);
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.startsWith("/public") ||
                path.equals("/health");
    }
}