package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final ItemService itemService;
    private final OrderService orderService;
    private final CartService cartService;
    private final TransactionalOperator transactionalOperator;

    public CheckoutService(ItemService itemService,
                           OrderService orderService,
                           CartService cartService,
                           ReactiveTransactionManager transactionManager) {
        this.itemService = itemService;
        this.orderService = orderService;
        this.cartService = cartService;
        this.transactionalOperator = TransactionalOperator.create(transactionManager);
    }

    public Mono<Order> createOrderFromCart(WebSession session) {
        return cartService.getCartItems(session)
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        log.warn("Attempted to create order with empty cart for session: {}", session.getId());
                        return Mono.error(new RuntimeException("Корзина пуста"));
                    }

                    log.info("Creating order for session: {}. Items in cart: {}", session.getId(), cartItems.size());

                    return Mono.defer(() -> executeOrderCreation(cartItems))
                            .flatMap(order -> cartService.clearCart(session)
                                    .thenReturn(order))
                            .as(transactionalOperator::transactional);
                });
    }

    private Mono<Order> executeOrderCreation(Map<Long, Integer> cartItems) {
        // Шаг 1: Блокируем и проверяем товары
        return lockAndValidateItems(cartItems)
                // Шаг 2: Создаем и сохраняем заказ
                .then(createAndSaveOrder(cartItems));
    }

    private Mono<Void> lockAndValidateItems(Map<Long, Integer> cartItems) {
        return Flux.fromIterable(cartItems.entrySet())
                .flatMap(entry -> itemService.decrementStock(entry.getKey(), entry.getValue())
                        .onErrorMap(RuntimeException.class, ex ->
                            new RuntimeException("Ошибка при проверке товара: " + ex.getMessage())))
                .then();
    }

    private Mono<Order> createAndSaveOrder(Map<Long, Integer> cartItems) {
        // Сначала создаем заказ
        // Вычисляем общую сумму реактивно через ItemService
        return Flux.fromIterable(cartItems.entrySet())
                .flatMap(entry -> itemService.getItemById(entry.getKey())
                        .map(item -> item.getPrice() * entry.getValue()))
                .reduce(0L, Long::sum)
                .flatMap(totalSum -> {
                    Order order = new Order(totalSum);
                    return orderService.saveOrderOnly(order);
                })
                .flatMap(savedOrder -> {
                    // Остатки уже были списаны в lockAndValidateItems, теперь создаем OrderItem
                    return Flux.fromIterable(cartItems.entrySet())
                            .flatMap(entry -> itemService.getItemById(entry.getKey())
                                    .flatMap(item -> {
                                        // Создаем OrderItem с orderId
                                        OrderItem orderItem = new OrderItem();
                                        orderItem.setOrderId(savedOrder.getId());
                                        orderItem.setTitle(item.getTitle());
                                        orderItem.setDescription(item.getDescription());
                                        orderItem.setImgPath(item.getImgPath());
                                        orderItem.setPrice(item.getPrice());
                                        orderItem.setCount(entry.getValue());

                                        return Mono.just(orderItem);
                                    }))
                            .collectList()
                            .flatMap(orderItems -> orderService.saveOrderItems(orderItems)
                                    .thenReturn(savedOrder));
                });
    }
}