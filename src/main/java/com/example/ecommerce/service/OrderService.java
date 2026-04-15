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

    /**
     * Получение списка заказов (без деталей)
     */
    public Flux<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .map(OrderDTO::summaryFromEntity);
    }

    /**
     * Получение заказа с деталями (позициями)
     */
    public Mono<OrderDTO> getOrderWithDetails(Long id) {
        return orderRepository.findById(id)
                .flatMap(order -> orderItemRepository.findAllByOrderId(order.getId())
                        .map(OrderItemDTO::fromEntity)
                        .collectList()
                        .map(items -> OrderDTO.fromEntity(order, items))
                );
    }

    /**
     * Сохранение заказа и его позиций
     */
    public Mono<Order> saveOrder(Order order, List<OrderItem> items) {
        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    // Сохраняем все позиции заказа с установленным orderId
                    items.forEach(item -> item.setOrderId(savedOrder.getId()));
                    return orderItemRepository.saveAll(items)
                            .then()
                            .thenReturn(savedOrder);
                });
    }

    /**
     * Сохранение только заказа (без позиций)
     */
    public Mono<Order> saveOrderOnly(Order order) {
        return orderRepository.save(order);
    }
}