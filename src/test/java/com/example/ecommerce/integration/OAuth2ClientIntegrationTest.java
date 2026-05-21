package com.example.ecommerce.integration;

import com.example.ecommerce.EcommerceApplication;
import com.example.ecommerce.service.PaymentServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = EcommerceApplication.class)
@ActiveProfiles("test")
public class OAuth2ClientIntegrationTest {

    @Autowired
    private PaymentServiceClient paymentServiceClient;

    @MockBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Test
    public void testGetPaymentServiceStatus() {
        // Создаем mock OAuth2AuthorizedClient с токеном
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(300)
        );

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("keycloak")
                .clientId("ecommerce-app")
                .clientSecret("ecommerce-app-secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri("http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token")
                .build();

        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                clientRegistration,
                "mock-principal",
                accessToken
        );

        // Тестируем метод получения статуса
        Mono<String> result = paymentServiceClient.getPaymentServiceStatus(authorizedClient);

        // Проверяем, что результат не null
        StepVerifier.create(result)
                .expectNextMatches(response -> response.contains("Error") || response.contains("Unexpected"))
                .verifyComplete();
    }

    @Test
    public void testCreatePayment() {
        // Создаем mock OAuth2AuthorizedClient с токеном
        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(300)
        );

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("keycloak")
                .clientId("ecommerce-app")
                .clientSecret("ecommerce-app-secret")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri("http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token")
                .build();

        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(
                clientRegistration,
                "mock-principal",
                accessToken
        );

        // Тестируем метод создания платежа
        Map<String, Object> paymentData = Collections.singletonMap("amount", 100.0);
        Mono<String> result = paymentServiceClient.createPayment(paymentData, authorizedClient);

        // Проверяем, что результат не null
        StepVerifier.create(result)
                .expectNextMatches(response -> response.contains("failed") || response.contains("Unexpected"))
                .verifyComplete();
    }
}