package com.example.ecommerce.payment.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class CustomJwtAuthenticationConverterTest {

    private CustomJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new CustomJwtAuthenticationConverter();
    }

    @Test
    void testConvertWithScopeString() {
        // Создаем JWT с scope в виде строки
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", "payment-service");

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claims(c -> c.putAll(claims))
                .build();

        // Проверяем конвертацию
        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    Collection<GrantedAuthority> authorities = authentication.getAuthorities();
                    assertThat(authorities).contains(new SimpleGrantedAuthority("SCOPE_payment-service"));
                })
                .verifyComplete();
    }

    @Test
    void testConvertWithScopeList() {
        // Создаем JWT с scope в виде списка
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", Arrays.asList("payment-service", "read"));

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claims(c -> c.putAll(claims))
                .build();

        // Проверяем конвертацию
        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    Collection<GrantedAuthority> authorities = authentication.getAuthorities();
                    assertThat(authorities).contains(
                            new SimpleGrantedAuthority("SCOPE_payment-service"),
                            new SimpleGrantedAuthority("SCOPE_read")
                    );
                })
                .verifyComplete();
    }

    @Test
    void testConvertWithRealmRoles() {
        // Создаем JWT с realm_access.roles
        Map<String, Object> claims = new HashMap<>();
        Map<String, Object> realmAccess = new HashMap<>();
        realmAccess.put("roles", Arrays.asList("USER", "ADMIN"));
        claims.put("realm_access", realmAccess);

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .claims(c -> c.putAll(claims))
                .build();

        // Проверяем конвертацию
        StepVerifier.create(converter.convert(jwt))
                .assertNext(authentication -> {
                    assertThat(authentication).isInstanceOf(JwtAuthenticationToken.class);
                    Collection<GrantedAuthority> authorities = authentication.getAuthorities();
                    assertThat(authorities).contains(
                            new SimpleGrantedAuthority("ROLE_USER"),
                            new SimpleGrantedAuthority("ROLE_ADMIN")
                    );
                })
                .verifyComplete();
    }
}