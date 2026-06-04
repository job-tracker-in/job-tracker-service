package com.portfolio.job_tracker_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Slf4j
@Configuration
public class SupabaseJwtConfig {

    @Value("${supabase.jwks-uri}")
    private String jwksUri;

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder delegate = NimbusJwtDecoder.withJwkSetUri(jwksUri).build();
        return token -> {
            try {
                Jwt jwt = delegate.decode(token);
                log.info("JWT decoded successfully for sub={}", jwt.getSubject());
                return jwt;
            } catch (JwtException e) {
                log.error("JWT decode failed: {}", e.getMessage(), e);
                throw e;
            }
        };
    }
}
