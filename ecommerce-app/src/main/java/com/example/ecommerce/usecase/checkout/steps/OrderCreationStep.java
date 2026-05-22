package com.example.ecommerce.usecase.checkout.steps;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.usecase.checkout.CheckoutContext;
import com.example.ecommerce.usecase.checkout.CheckoutStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Шаг создания заказа
 */
@Component
public class OrderCreationStep implements CheckoutStep {

    private static final Logger log = LoggerFactory.getLogger(OrderCreationStep.class);

    private final OrderService orderService;

    public OrderCreationStep(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Mono<CheckoutContext> execute(CheckoutContext context) {
        log.info("Executing order creation step");

        Order order = new Order(context.getTotalSum());
        log.debug("Creating order with total sum: {}", order.getTotalSum());

        return orderService.saveOrderOnly(order)
                .doOnNext(savedOrder -> log.info("Order created successfully with ID: {}", savedOrder.getId()))
                .map(savedOrder -> {
                    context.setOrder(savedOrder);
                    return context;
                });
    }
}