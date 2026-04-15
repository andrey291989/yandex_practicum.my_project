package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final ItemRepository itemRepository;
    private final OrderService orderService;
    private final CartService cartService;

    public CheckoutService(ItemRepository itemRepository,
                           OrderService orderService,
                           CartService cartService) {
        this.itemRepository = itemRepository;
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @Transactional
    public Mono<Order> createOrderFromCart(WebSession session) {
        return cartService.getCartItems(session)
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        log.warn("Attempted to create order with empty cart for session: {}", session.getId());
                        return Mono.error(new RuntimeException("Корзина пуста"));
                    }

                    log.info("Creating order for session: {}. Items in cart: {}", session.getId(), cartItems.size());

                    return validateStockAvailability(cartItems)
                            .then(createOrder(cartItems))
                            .flatMap(order -> orderService.saveOrderOnly(order))
                            .flatMap(order -> saveOrderItems(order.getId(), cartItems)
                                    .thenReturn(order))
                            .flatMap(order -> cartService.clearCart(session)
                                    .thenReturn(order));
                });
    }

    private Mono<Void> validateStockAvailability(Map<Long, Integer> cartItems) {
        return Flux.fromIterable(cartItems.entrySet())
                .flatMap(entry -> itemRepository.findById(entry.getKey())
                        .switchIfEmpty(Mono.error(new RuntimeException("Товар с id " + entry.getKey() + " не найден")))
                        .flatMap(item -> {
                            int requestedQuantity = entry.getValue();
                            if (item.getCount() < requestedQuantity) {
                                log.warn("Insufficient stock for item {}. Available: {}, Requested: {}",
                                        item.getTitle(), item.getCount(), requestedQuantity);
                                return Mono.error(new RuntimeException("Недостаточно товара '" + item.getTitle() +
                                        "' на складе. Доступно: " + item.getCount() +
                                        ", запрошено: " + requestedQuantity));
                            }
                            return Mono.just(item);
                        }))
                .then();
    }

    private Mono<Order> createOrder(Map<Long, Integer> cartItems) {
        Order order = new Order();

        return Flux.fromIterable(cartItems.entrySet())
                .flatMap(entry -> itemRepository.findById(entry.getKey())
                        .flatMap(item -> {
                            int requestedQuantity = entry.getValue();
                            int oldStock = item.getCount();
                            int newCount = oldStock - requestedQuantity;
                            item.setCount(newCount);

                            log.info("Updated stock: {} | Old: {} | Sold: {} | New: {}",
                                    item.getTitle(), oldStock, requestedQuantity, newCount);

                            return itemRepository.save(item)
                                    .thenReturn(item.getPrice() * requestedQuantity);
                        }))
                .reduce(0L, Long::sum)
                .flatMap(totalSum -> {
                    order.setTotalSum(totalSum);
                    return Mono.just(order);
                });
    }

    private Mono<Void> saveOrderItems(Long orderId, Map<Long, Integer> cartItems) {
        return Flux.fromIterable(cartItems.entrySet())
                .flatMap(entry -> itemRepository.findById(entry.getKey())
                        .map(item -> {
                            OrderItem orderItem = new OrderItem();
                            orderItem.setOrderId(orderId);
                            orderItem.setTitle(item.getTitle());
                            orderItem.setDescription(item.getDescription());
                            orderItem.setImgPath(item.getImgPath());
                            orderItem.setPrice(item.getPrice());
                            orderItem.setCount(entry.getValue());
                            return orderItem;
                        }))
                .then();
    }
}