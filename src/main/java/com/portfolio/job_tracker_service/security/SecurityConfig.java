package com.portfolio.job_tracker_service.security;

import com.portfolio.job_tracker_service.config.KeycloakClientRoleConverter;
import com.portfolio.job_tracker_service.config.KeycloakRealmRoleConverter;
import com.portfolio.job_tracker_service.config.UserAutoCreateFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final KeycloakClientRoleConverter keycloakClientRoleConverter;
    private final KeycloakRealmRoleConverter keycloakRealmRoleConverter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final UserAutoCreateFilter userAutoCreateFilter;

    public SecurityConfig(
            KeycloakClientRoleConverter keycloakClientRoleConverter,
            KeycloakRealmRoleConverter keycloakRealmRoleConverter,
            CorsConfigurationSource corsConfigurationSource, UserAutoCreateFilter userAutoCreateFilter) {
        this.keycloakClientRoleConverter = keycloakClientRoleConverter;
        this.keycloakRealmRoleConverter = keycloakRealmRoleConverter;
        this.corsConfigurationSource = corsConfigurationSource;
        this.userAutoCreateFilter = userAutoCreateFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> combined = new ArrayList<>();
            combined.addAll(Objects.requireNonNull(keycloakClientRoleConverter.convert(jwt)));
            combined.addAll(Objects.requireNonNull(keycloakRealmRoleConverter.convert(jwt)));
            return combined;
        });

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Enable CORS
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/actuator/**", "/health/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterAfter(userAutoCreateFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}