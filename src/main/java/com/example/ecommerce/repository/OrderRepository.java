package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    /**
     * Найти все заказы, отсортированные по дате создания (новые сверху)
     */
    Flux<Order> findAllByOrderByCreatedAtDesc();

    /**
     * Найти заказ по ID с возможностью блокировки
     */
    Mono<Order> findById(Long id);
}