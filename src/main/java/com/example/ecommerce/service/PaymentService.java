package com.example.ecommerce.service;

import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.payment.model.CreatePaymentRequest;
import com.example.ecommerce.payment.model.PaymentResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final TransactionalOperator transactionalOperator;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository,
                         OrderService orderService,
                         ReactiveTransactionManager transactionManager) {
        this.paymentRepository = paymentRepository;
        this.orderService = orderService;
        this.transactionalOperator = TransactionalOperator.create(transactionManager);
    }

    /**
     * Создает новый платеж
     * @param request данные для создания платежа
     * @return Mono с результатом платежа
     */
    public Mono<PaymentResponse> createPayment(CreatePaymentRequest request) {
        return Mono.defer(() -> {
            // Проверяем существование заказа
            return orderService.getOrderById(request.getOrderId())
                    .switchIfEmpty(Mono.error(new RuntimeException("Order not found: " + request.getOrderId())))
                    .flatMap(order -> {
                        // Создаем платеж
                        Payment payment = new Payment(
                                request.getOrderId(),
                                request.getUserId(),
                                request.getAmount(),
                                request.getDescription()
                        );

                        // Сохраняем платеж и обновляем статус заказа
                        return paymentRepository.save(payment)
                                .flatMap(savedPayment -> {
                                    // Здесь можно добавить логику списания средств
                                    // Для демонстрации просто помечаем платеж как завершенный
                                    savedPayment.setStatus("COMPLETED");
                                    return paymentRepository.save(savedPayment);
                                })
                                .as(transactionalOperator::transactional);
                    });
        }).map(this::toPaymentResponse);
    }

    /**
     * Получает статус платежа по ID
     * @param paymentId ID платежа
     * @return Mono с информацией о платеже
     */
    public Mono<PaymentResponse> getPaymentStatus(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new RuntimeException("Payment not found: " + paymentId)))
                .map(this::toPaymentResponse);
    }

    /**
     * Преобразует сущность Payment в PaymentResponse
     * @param payment сущность платежа
     * @return PaymentResponse
     */
    private PaymentResponse toPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setStatus(PaymentResponse.StatusEnum.valueOf(payment.getStatus()));
        response.setDescription(payment.getDescription());

        // Преобразуем LocalDateTime в OffsetDateTime
        if (payment.getCreatedAt() != null) {
            response.setCreatedAt(payment.getCreatedAt().atOffset(java.time.ZoneOffset.UTC));
        }
        if (payment.getUpdatedAt() != null) {
            response.setUpdatedAt(payment.getUpdatedAt().atOffset(java.time.ZoneOffset.UTC));
        }

        return response;
    }
}