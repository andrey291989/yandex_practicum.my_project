package com.example.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.Instant;

import static org.mockito.Mockito.*;

class PaymentServiceClientTest {

    private PaymentServiceClient paymentServiceClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        paymentServiceClient = new PaymentServiceClient(webClientBuilder);
    }

    @Test
    void testConstructor() {
        // Проверяем, что объект создается успешно
        assert paymentServiceClient != null;
    }

    @Test
    void testGetPaymentServiceStatus() {
        OAuth2AuthorizedClient authorizedClient = createMockAuthorizedClient();

        // Проверяем, что метод не выбрасывает исключения при вызове
        assertDoesNotThrow(() -> {
            paymentServiceClient.getPaymentServiceStatus(authorizedClient);
        });
    }

    @Test
    void testCreatePayment() {
        OAuth2AuthorizedClient authorizedClient = createMockAuthorizedClient();

        // Проверяем, что метод не выбрасывает исключения при вызове
        assertDoesNotThrow(() -> {
            paymentServiceClient.createPayment(java.util.Collections.emptyMap(), authorizedClient);
        });
    }

    private OAuth2AuthorizedClient createMockAuthorizedClient() {
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

        return new OAuth2AuthorizedClient(clientRegistration, "mock-principal", accessToken);
    }

    private void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw new AssertionError("Expected no exception, but got: " + e.getClass().getSimpleName());
        }
    }
}