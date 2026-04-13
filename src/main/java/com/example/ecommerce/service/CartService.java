package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private static final String CART_SESSION_KEY = "CART_ITEMS";

    private final ItemRepository itemRepository;

    public CartService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    private Map<Long, Integer> getCartFromSession(HttpSession session) {
        Map<Long, Integer> cartItems = (Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY);
        if (cartItems == null) {
            cartItems = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cartItems);
            log.debug("Created new cart for session: {}", session.getId());
        }
        return cartItems;
    }

    public void addToCart(HttpSession session, Long itemId) {
        Map<Long, Integer> cartItems = getCartFromSession(session);
        cartItems.merge(itemId, 1, Integer::sum);
        log.info("Added item {} to cart. New quantity: {}. Session: {}",
                itemId, cartItems.get(itemId), session.getId());
    }

    public void removeFromCart(HttpSession session, Long itemId) {
        Map<Long, Integer> cartItems = getCartFromSession(session);
        cartItems.remove(itemId);
        log.info("Removed item {} from cart. Session: {}", itemId, session.getId());
    }

    public void decreaseQuantity(HttpSession session, Long itemId) {
        Map<Long, Integer> cartItems = getCartFromSession(session);
        cartItems.computeIfPresent(itemId, (id, count) -> {
            if (count <= 1) {
                log.debug("Item {} quantity would become 0, removing from cart", itemId);
                return null;
            }
            return count - 1;
        });
        log.info("Decreased quantity of item {}. Session: {}", itemId, session.getId());
    }

    public Map<Long, Integer> getCartItems(HttpSession session) {
        Map<Long, Integer> cartItems = getCartFromSession(session);
        log.debug("Retrieved cart items for session {}. Size: {}", session.getId(), cartItems.size());
        return new HashMap<>(cartItems);
    }

    public void clearCart(HttpSession session) {
        Map<Long, Integer> cartItems = getCartFromSession(session);
        cartItems.clear();
        log.info("Cleared cart for session: {}", session.getId());
    }

    public boolean checkStockAvailability(Long itemId, int requestedQuantity) {
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            log.warn("Stock check failed: Item {} not found", itemId);
            return false;
        }
        boolean available = item.getCount() >= requestedQuantity;
        if (!available) {
            log.debug("Insufficient stock for item {}. Available: {}, Requested: {}",
                    item.getTitle(), item.getCount(), requestedQuantity);
        }
        return available;
    }

    public int getAvailableStock(Long itemId) {
        Item item = itemRepository.findById(itemId).orElse(null);
        int stock = item != null ? item.getCount() : 0;
        log.debug("Available stock for item {}: {}", itemId, stock);
        return stock;
    }
}