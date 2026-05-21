package com.example.ecommerce.payment.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    private final ReactiveJwtAuthenticationConverter defaultConverter = new ReactiveJwtAuthenticationConverter();

    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        // Извлекаем роли из JWT
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        // Создаем JwtAuthenticationToken с извлеченными авторитетами
        return Mono.just(new JwtAuthenticationToken(jwt, authorities));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Извлекаем scope из JWT
        Object scope = jwt.getClaims().get("scope");
        if (scope instanceof String) {
            String scopeStr = (String) scope;
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + scopeStr));
        } else if (scope instanceof List) {
            List<String> scopes = (List<String>) scope;
            authorities.addAll(scopes.stream()
                    .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
                    .collect(Collectors.toList()));
        }

        // Извлекаем realm_access.roles если есть
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList()));
        }

        // Извлекаем resource_access если есть
        Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get("resource_access");
        if (resourceAccess != null) {
            resourceAccess.forEach((key, value) -> {
                if (value instanceof Map) {
                    Map<String, Object> resource = (Map<String, Object>) value;
                    if (resource.containsKey("roles")) {
                        List<String> roles = (List<String>) resource.get("roles");
                        authorities.addAll(roles.stream()
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList()));
                    }
                }
            });
        }

        return authorities;
    }
}