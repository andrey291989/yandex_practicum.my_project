package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OrderItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {

    /**
     * Найти все позиции заказа по ID заказа
     */
    Flux<OrderItem> findAllByOrderId(Long orderId);

    /**
     * Удалить все позиции заказа по ID заказа
     */
    Mono<Void> deleteAllByOrderId(Long orderId);

    /**
     * Подсчитать сумму заказа по его позициям
     */
    @Query("SELECT COALESCE(SUM(price * count), 0) FROM order_items WHERE order_id = :orderId")
    Mono<Long> calculateTotalSumByOrderId(Long orderId);
}