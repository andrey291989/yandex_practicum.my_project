package com.example.ecommerce.controller;

import com.example.ecommerce.service.PaymentServiceClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/external-payments")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ExternalPaymentController {

    private final PaymentServiceClient paymentServiceClient;

    public ExternalPaymentController(PaymentServiceClient paymentServiceClient) {
        this.paymentServiceClient = paymentServiceClient;
    }

    @GetMapping("/status")
    public Mono<String> getPaymentServiceStatus(
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        return paymentServiceClient.getPaymentServiceStatus(authorizedClient)
                .flatMap(status -> {
                    model.addAttribute("status", status);
                    return Mono.just("payment-status");
                });
    }

    @PostMapping("/create")
    public Mono<String> createPayment(
            @RequestParam Double amount,
            @RequestParam String currency,
            @RequestParam String description,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("amount", amount);
        paymentData.put("currency", currency);
        paymentData.put("description", description);

        return paymentServiceClient.createPayment(paymentData, authorizedClient)
                .flatMap(result -> {
                    model.addAttribute("result", result);
                    return Mono.just("payment-result");
                });
    }

    @GetMapping("/status/{paymentId}")
    public Mono<String> getPaymentStatus(
            @PathVariable String paymentId,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        return paymentServiceClient.getPaymentStatus(paymentId, authorizedClient)
                .flatMap(status -> {
                    model.addAttribute("paymentId", paymentId);
                    model.addAttribute("status", status);
                    return Mono.just("payment-details");
                });
    }

    @PostMapping("/confirm/{paymentId}")
    public Mono<String> confirmPayment(
            @PathVariable String paymentId,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient,
            Model model) {

        return paymentServiceClient.confirmPayment(paymentId, authorizedClient)
                .flatMap(result -> {
                    model.addAttribute("paymentId", paymentId);
                    model.addAttribute("result", result);
                    return Mono.just("payment-confirmation");
                });
    }
}