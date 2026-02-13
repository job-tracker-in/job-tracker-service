package com.portfolio.job_tracker_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakClientRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.containsKey("jobtracker-api")) {
            Map<String, Object> jobTracker = (Map<String, Object>) resourceAccess.get("jobtracker-api");
            if(jobTracker != null && jobTracker.containsKey("roles")){
                List<String> roles = (List<String>) jobTracker.get("roles");
                roles.forEach(r ->
                        authorities.add(new SimpleGrantedAuthority(r)));}

        }
        return authorities;
    }
}
