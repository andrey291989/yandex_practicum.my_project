package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.dto.OrderItemDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public Flux<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public Mono<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    /**
     * Получить заказ с полной информацией, включая позиции
     * @param id ID заказа
     * @return_mono с заказом и его позициями
     */
    public Mono<OrderDTO> getOrderWithItemsById(Long id) {
        return orderRepository.findById(id)
                .flatMap(order ->
                    orderItemRepository.findAllByOrderId(order.getId())
                        .map(OrderItemDTO::fromEntity)
                        .collectList()
                        .map(items -> OrderDTO.fromEntityWithItems(order, items))
                );
    }

    public Mono<Order> saveOrderOnly(Order order) {
        log.info("Saving order: totalSum={}", order.getTotalSum());
        return orderRepository.save(order);
    }

    public Mono<Void> saveOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return Mono.empty();
        }
        return orderItemRepository.saveAll(orderItems).then();
    }
}