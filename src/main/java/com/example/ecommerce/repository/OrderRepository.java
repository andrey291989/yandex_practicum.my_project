package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderRepository extends R2dbcRepository<Order, Long> {

    Flux<Order> findAllByOrderByCreatedAtDesc();
}