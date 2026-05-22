package com.example.ecommerce.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payment")
public class ExternalPaymentController {

    @GetMapping("/status")
    public Mono<String> getPaymentStatus() {
        return Mono.just("Payment service is operational");
    }

    @GetMapping("/protected/status")
    public Mono<String> getProtectedPaymentStatus() {
        return Mono.just("Protected payment service is operational - accessed with valid token");
    }

    @GetMapping("/public/health")
    public Mono<String> getPublicHealth() {
        return Mono.just("Payment service is healthy and running");
    }
}