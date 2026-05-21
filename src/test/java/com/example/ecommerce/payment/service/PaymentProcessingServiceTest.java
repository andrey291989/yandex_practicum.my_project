package com.example.ecommerce.payment.service;

import com.example.ecommerce.payment.model.CreatePaymentRequest;
import com.example.ecommerce.payment.model.PaymentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentProcessingServiceTest {

    private PaymentProcessingService paymentProcessingService;

    @BeforeEach
    void setUp() {
        paymentProcessingService = new PaymentProcessingService();
    }

    @Test
    void testProcessPayment() {
        // Создаем запрос на платеж
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(1L);
        request.setUserId(1L);
        request.setAmount(1000L);
        request.setDescription("Test payment");

        // Проверяем обработку платежа
        StepVerifier.create(paymentProcessingService.processPayment(request))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isNotNull();
                    assertThat(response.getOrderId()).isEqualTo(1L);
                    assertThat(response.getUserId()).isEqualTo(1L);
                    assertThat(response.getAmount()).isEqualTo(1000L);
                    assertThat(response.getDescription()).isEqualTo("Test payment");
                    assertThat(response.getStatus()).isEqualTo(PaymentResponse.StatusEnum.COMPLETED);
                    assertThat(response.getCreatedAt()).isNotNull();
                    assertThat(response.getUpdatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void testGetPaymentStatus() {
        // Сначала создаем платеж
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setOrderId(1L);
        request.setUserId(1L);
        request.setAmount(1000L);
        request.setDescription("Test payment");

        PaymentResponse createdPayment = paymentProcessingService.processPayment(request).block();
        Long paymentId = createdPayment.getId();

        // Проверяем получение статуса платежа
        StepVerifier.create(paymentProcessingService.getPaymentStatus(paymentId))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getId()).isEqualTo(paymentId);
                    assertThat(response.getOrderId()).isEqualTo(1L);
                    assertThat(response.getUserId()).isEqualTo(1L);
                    assertThat(response.getAmount()).isEqualTo(1000L);
                    assertThat(response.getStatus()).isEqualTo(PaymentResponse.StatusEnum.COMPLETED);
                })
                .verifyComplete();
    }

    @Test
    void testGetPaymentStatusNotFound() {
        // Проверяем получение несуществующего платежа
        StepVerifier.create(paymentProcessingService.getPaymentStatus(999L))
                .expectError(RuntimeException.class)
                .verify();
    }
}