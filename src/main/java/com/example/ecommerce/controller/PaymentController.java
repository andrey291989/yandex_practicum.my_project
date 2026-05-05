package com.example.ecommerce.controller;

import com.example.ecommerce.payment.api.DefaultApi;
import com.example.ecommerce.payment.model.CreatePaymentRequest;
import com.example.ecommerce.payment.model.ErrorResponse;
import com.example.ecommerce.payment.model.PaymentResponse;
import com.example.ecommerce.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestController
@Tag(name = "Payment", description = "Operations related to payment processing")
public class PaymentController implements DefaultApi {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(
        summary = "Create payment",
        description = "Creates a new payment request"
    )
    @ApiResponse(responseCode = "201", description = "Payment created successfully",
                 content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = PaymentResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request data",
                 content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)))
    @Override
    public Mono<ResponseEntity<PaymentResponse>> apiPaymentsPost(
            @Parameter(description = "Payment creation request") Mono<CreatePaymentRequest> createPaymentRequest,
            ServerWebExchange exchange) {
        return createPaymentRequest
                .flatMap(request -> paymentService.createPayment(request))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .onErrorResume(RuntimeException.class, ex -> {
                    ErrorResponse error = new ErrorResponse();
                    error.setError(ex.getMessage());
                    error.setStatus(400);
                    error.setTimestamp(OffsetDateTime.now(ZoneOffset.UTC));
                    return Mono.just(ResponseEntity.badRequest().body(null));
                });
    }

    @Operation(
        summary = "Get payment status",
        description = "Retrieves the status of a specific payment by ID"
    )
    @ApiResponse(responseCode = "200", description = "Payment status retrieved successfully",
                 content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = PaymentResponse.class)))
    @ApiResponse(responseCode = "404", description = "Payment not found",
                 content = @Content(mediaType = "application/json",
                          schema = @Schema(implementation = ErrorResponse.class)))
    @Override
    public Mono<ResponseEntity<PaymentResponse>> apiPaymentsPaymentIdGet(
            @Parameter(description = "Payment ID") Long paymentId,
            ServerWebExchange exchange) {
        return paymentService.getPaymentStatus(paymentId)
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