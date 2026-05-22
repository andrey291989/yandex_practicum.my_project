package com.example.ecommerce.usecase.checkout.steps;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.service.ItemService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.usecase.checkout.CheckoutContext;
import com.example.ecommerce.usecase.checkout.CheckoutStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Шаг создания позиций заказа
 */
@Component
public class OrderItemsCreationStep implements CheckoutStep {

    private static final Logger log = LoggerFactory.getLogger(OrderItemsCreationStep.class);

    private final ItemService itemService;
    private final OrderService orderService;

    public OrderItemsCreationStep(ItemService itemService, OrderService orderService) {
        this.itemService = itemService;
        this.orderService = orderService;
    }

    @Override
    public Mono<CheckoutContext> execute(CheckoutContext context) {
        log.info("Executing order items creation step");

        Long orderId = context.getOrder().getId();
        Map<Long, Integer> cartItems = context.getCartItems();

        return Flux.fromIterable(cartItems.entrySet())
                .flatMap(entry -> {
                    Long itemId = entry.getKey();
                    Integer quantity = entry.getValue();

                    log.debug("Creating order item for item {} with quantity {}", itemId, quantity);
                    return itemService.getItemById(itemId)
                            .map(item -> createOrderItem(item, orderId, quantity));
                })
                .collectList()
                .flatMap(orderItems -> {
                    log.info("Saving {} order items", orderItems.size());
                    return orderService.saveOrderItems(orderItems)
                            .thenReturn(context);
                });
    }

    private OrderItem createOrderItem(Item item, Long orderId, Integer quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setTitle(item.getTitle());
        orderItem.setDescription(item.getDescription());
        orderItem.setImgPath(item.getImgPath());
        orderItem.setPrice(item.getPrice());
        orderItem.setCount(quantity);
        return orderItem;
    }
}