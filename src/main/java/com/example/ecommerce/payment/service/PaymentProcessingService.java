package com.example.ecommerce.payment.service;

import com.example.ecommerce.payment.model.CreatePaymentRequest;
import com.example.ecommerce.payment.model.PaymentResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class PaymentProcessingService {

    private final AtomicLong idGenerator = new AtomicLong(1);
    private final ConcurrentHashMap<Long, PaymentResponse> payments = new ConcurrentHashMap<>();

    public Mono<PaymentResponse> processPayment(CreatePaymentRequest request) {
        return Mono.fromCallable(() -> {
            PaymentResponse response = new PaymentResponse();
            long id = idGenerator.getAndIncrement();

            response.setId(id);
            response.setOrderId(request.getOrderId());
            response.setUserId(request.getUserId());
            response.setAmount(request.getAmount());
            response.setDescription(request.getDescription());
            response.setStatus(PaymentResponse.StatusEnum.COMPLETED);
            response.setCreatedAt(OffsetDateTime.now());
            response.setUpdatedAt(OffsetDateTime.now());

            payments.put(id, response);

            return response;
        });
    }

    public Mono<PaymentResponse> getPaymentStatus(Long paymentId) {
        return Mono.fromCallable(() -> {
            PaymentResponse payment = payments.get(paymentId);
            if (payment == null) {
                throw new RuntimeException("Payment not found: " + paymentId);
            }
            return payment;
        });
    }
}