package com.example.ecommerce.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://localhost:8082") // Платежный сервис
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Получить статус платежного сервиса
     */
    public Mono<String> getPaymentServiceStatus(@RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .get()
                .uri("/api/payment/status")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just("Error: " + ex.getMessage());
                })
                .onErrorResume(Exception.class, ex -> {
                    return Mono.just("Unexpected error: " + ex.getMessage());
                });
    }

    /**
     * Создать платеж
     */
    public Mono<String> createPayment(Map<String, Object> paymentData, @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .post()
                .uri("/api/payment/create")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .bodyValue(paymentData)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just("Payment creation failed: " + ex.getMessage());
                })
                .onErrorResume(Exception.class, ex -> {
                    return Mono.just("Unexpected error during payment creation: " + ex.getMessage());
                });
    }

    /**
     * Получить статус платежа
     */
    public Mono<String> getPaymentStatus(String paymentId, @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .get()
                .uri("/api/payment/status/{paymentId}", paymentId)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just("Failed to get payment status: " + ex.getMessage());
                })
                .onErrorResume(Exception.class, ex -> {
                    return Mono.just("Unexpected error getting payment status: " + ex.getMessage());
                });
    }

    /**
     * Подтвердить платеж
     */
    public Mono<String> confirmPayment(String paymentId, @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        return webClient
                .post()
                .uri("/api/payment/{paymentId}/confirm", paymentId)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    return Mono.just("Payment confirmation failed: " + ex.getMessage());
                })
                .onErrorResume(Exception.class, ex -> {
                    return Mono.just("Unexpected error during payment confirmation: " + ex.getMessage());
                });
    }
}