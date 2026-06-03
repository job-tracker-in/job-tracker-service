package com.portfolio.job_tracker_service.security;

import com.portfolio.job_tracker_service.config.SupabaseRoleConverter;
import com.portfolio.job_tracker_service.config.UserAutoCreateFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final SupabaseRoleConverter supabaseRoleConverter;
    private final CorsConfigurationSource corsConfigurationSource;
    private final UserAutoCreateFilter userAutoCreateFilter;

    public SecurityConfig(SupabaseRoleConverter supabaseRoleConverter,
                          CorsConfigurationSource corsConfigurationSource,
                          UserAutoCreateFilter userAutoCreateFilter) {
        this.supabaseRoleConverter = supabaseRoleConverter;
        this.corsConfigurationSource = corsConfigurationSource;
        this.userAutoCreateFilter = userAutoCreateFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(supabaseRoleConverter);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request ->
                        request.requestMatchers("/actuator/health/**", "/actuator/prometheus").permitAll()
                                .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .addFilterAfter(userAutoCreateFilter, BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}
