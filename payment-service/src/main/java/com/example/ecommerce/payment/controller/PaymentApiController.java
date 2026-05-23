package com.example.ecommerce.payment.controller;

import com.example.ecommerce.payment.api.DefaultApi;
import com.example.ecommerce.payment.model.CreatePaymentRequest;
import com.example.ecommerce.payment.model.ErrorResponse;
import com.example.ecommerce.payment.model.PaymentResponse;
import com.example.ecommerce.payment.service.PaymentProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@CrossOrigin
public class PaymentApiController implements DefaultApi {

    private final PaymentProcessingService paymentProcessingService;

    @Autowired
    public PaymentApiController(PaymentProcessingService paymentProcessingService) {
        this.paymentProcessingService = paymentProcessingService;
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> apiPaymentsPost(
            Mono<CreatePaymentRequest> createPaymentRequest,
            ServerWebExchange exchange) {

        return createPaymentRequest
                .flatMap(request -> paymentProcessingService.processPayment(request))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(RuntimeException.class, ex -> {
                    ErrorResponse error = new ErrorResponse();
                    error.setError(ex.getMessage());
                    error.setStatus(400);
                    error.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                    return Mono.just(ResponseEntity.badRequest().body(null));
                });
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> apiPaymentsPaymentIdGet(
            Long paymentId,
            ServerWebExchange exchange) {

        return paymentProcessingService.getPaymentStatus(paymentId)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(RuntimeException.class, ex -> {
                    ErrorResponse error = new ErrorResponse();
                    error.setError(ex.getMessage());
                    error.setStatus(404);
                    error.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }
}