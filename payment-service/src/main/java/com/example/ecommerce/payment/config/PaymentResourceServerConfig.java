package com.example.ecommerce.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class PaymentResourceServerConfig {

    @Bean
    public SecurityWebFilterChain paymentResourceServerSecurityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/api/payment/public/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .anyExchange().hasAuthority("SCOPE_payment-service") // Требуем scope payment-service
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter()))
                )
                .csrf(csrf -> csrf.disable())
                .build();
    }
}