package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Payment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentRepository extends R2dbcRepository<Payment, Long> {
    Mono<Payment> findByOrderId(Long orderId);
}