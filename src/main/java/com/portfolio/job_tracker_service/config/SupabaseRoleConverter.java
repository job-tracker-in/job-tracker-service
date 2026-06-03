package com.portfolio.job_tracker_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class SupabaseRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final List<GrantedAuthority> ALL_PERMISSIONS = List.of(
            new SimpleGrantedAuthority("application.read"),
            new SimpleGrantedAuthority("application.write"),
            new SimpleGrantedAuthority("application.delete"),
            new SimpleGrantedAuthority("company.manage")
    );

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        if ("authenticated".equals(role)) {
            return ALL_PERMISSIONS;
        }
        return List.of();
    }
}
