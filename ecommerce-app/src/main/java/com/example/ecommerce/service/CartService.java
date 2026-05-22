package com.example.ecommerce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final String CART_SESSION_KEY = "CART_ITEMS";

    /**
     * Получение корзины из сессии
     */
    private Map<Long, Integer> getOrCreateCart(WebSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttributes().get(CART_SESSION_KEY);
        if (cart == null) {
            cart = new ConcurrentHashMap<>();
            session.getAttributes().put(CART_SESSION_KEY, cart);
            log.debug("Created new cart for session: {}", session.getId());
        }
        return cart;
    }

    /**
     * Добавление товара в корзину
     */
    public Mono<Void> addToCart(WebSession session, Long itemId) {
        return Mono.defer(() -> {
            Map<Long, Integer> cart = getOrCreateCart(session);
            cart.merge(itemId, 1, Integer::sum);
            log.info("Added item {} to cart. New quantity: {}. Session: {}",
                    itemId, cart.get(itemId), session.getId());
            return Mono.empty();
        });
    }

    /**
     * Удаление товара из корзины
     */
    public Mono<Void> removeFromCart(WebSession session, Long itemId) {
        return Mono.defer(() -> {
            Map<Long, Integer> cart = getOrCreateCart(session);
            cart.remove(itemId);
            log.info("Removed item {} from cart. Session: {}", itemId, session.getId());
            return Mono.empty();
        });
    }

    /**
     * Уменьшение количества товара в корзине
     */
    public Mono<Void> decreaseQuantity(WebSession session, Long itemId) {
        return Mono.defer(() -> {
            Map<Long, Integer> cart = getOrCreateCart(session);
            cart.computeIfPresent(itemId, (id, count) -> {
                if (count <= 1) {
                    log.debug("Item {} quantity would become 0, removing from cart", itemId);
                    return null;
                }
                return count - 1;
            });
            log.info("Decreased quantity of item {}. Session: {}", itemId, session.getId());
            return Mono.empty();
        });
    }

    /**
     * Получение копии всех товаров в корзине
     */
    public Mono<Map<Long, Integer>> getCartItems(WebSession session) {
        return Mono.fromSupplier(() -> {
            Map<Long, Integer> cart = getOrCreateCart(session);
            return new HashMap<>(cart);
        });
    }

    /**
     * Очистка корзины
     */
    public Mono<Void> clearCart(WebSession session) {
        return Mono.defer(() -> {
            Map<Long, Integer> cart = getOrCreateCart(session);
            cart.clear();
            log.info("Cleared cart for session: {}", session.getId());
            return Mono.empty();
        });
    }
}