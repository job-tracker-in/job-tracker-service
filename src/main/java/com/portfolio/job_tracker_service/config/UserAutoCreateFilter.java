package com.portfolio.job_tracker_service.config;

import com.portfolio.job_tracker_service.model.UserDetails;
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
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAutoCreateFilter extends OncePerRequestFilter {

    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null &&
                    authentication.isAuthenticated() &&
                    authentication.getPrincipal() instanceof Jwt jwt) {

                String userId = jwt.getClaimAsString("sub");
                String email = jwt.getClaimAsString("email");

                if (userId != null && !userId.isBlank()) {
                    String firstName = extractFirstName(jwt);
                    String lastName = extractLastName(jwt);

                    UserDetails user = userService.findOrCreateUser(
                            new UserDetails(UUID.fromString(userId), firstName, lastName, email, ""));
                    request.setAttribute("currentUser", user);
                    log.debug("User loaded: {}", email);
                }
            }
        } catch (Exception e) {
            log.error("Error in UserAutoCreateFilter - continuing request processing", e);
        }

        filterChain.doFilter(request, response);
    }

    private String extractFirstName(Jwt jwt) {
        Map<String, Object> meta = jwt.getClaim("user_metadata");
        if (meta == null) return "";
        if (meta.containsKey("given_name")) return String.valueOf(meta.get("given_name"));
        String fullName = meta.containsKey("full_name")
                ? String.valueOf(meta.get("full_name"))
                : String.valueOf(meta.getOrDefault("name", ""));
        int space = fullName.indexOf(' ');
        return space > 0 ? fullName.substring(0, space) : fullName;
    }

    private String extractLastName(Jwt jwt) {
        Map<String, Object> meta = jwt.getClaim("user_metadata");
        if (meta == null) return "";
        if (meta.containsKey("family_name")) return String.valueOf(meta.get("family_name"));
        String fullName = meta.containsKey("full_name")
                ? String.valueOf(meta.get("full_name"))
                : String.valueOf(meta.getOrDefault("name", ""));
        int space = fullName.indexOf(' ');
        return space > 0 ? fullName.substring(space + 1) : "";
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/public");
    }
}
