package com.portfolio.job_tracker_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Configuration
public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> realmRoles = new ArrayList<>();
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if(realmAccess != null && realmAccess.containsKey("roles")){
            List<String> roles = (List<String>) realmAccess.get("roles");
            roles.forEach(i -> realmRoles.add(new SimpleGrantedAuthority(i)));
        }
        return realmRoles;
    }
}
