package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Service
@Transactional
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

    /**
     * Создание заказа на основе текущей корзины
     * Использует пессимистическую блокировку для защиты от race condition
     */
    public Order createOrderFromCart(HttpSession session) {
        Map<Long, Integer> cartItems = cartService.getCartItems(session);

        if (cartItems.isEmpty()) {
            log.warn("Attempted to create order with empty cart for session: {}", session.getId());
            throw new RuntimeException("Корзина пуста");
        }

        log.info("Creating order for session: {}. Items in cart: {}", session.getId(), cartItems.size());

        // Проверка наличия товаров на складе с блокировкой
        validateStockAvailabilityWithLock(cartItems);

        // Создание заказа и обновление остатков
        Order order = createOrderWithLock(cartItems);

        // Очищаем корзину после успешного создания заказа
        cartService.clearCart(session);

        log.info("Order created successfully. Order ID: {}, Session: {}", order.getId(), session.getId());

        return order;
    }

    /**
     * Проверка наличия всех товаров на складе с использованием блокировки
     * Это предотвращает race condition при параллельных покупках
     */
    private void validateStockAvailabilityWithLock(Map<Long, Integer> cartItems) {
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            // Используем блокировку для чтения актуального состояния
            Item item = itemRepository.findByIdWithLock(entry.getKey())
                    .orElseThrow(() -> {
                        log.error("Item {} not found in database", entry.getKey());
                        return new RuntimeException("Товар с id " + entry.getKey() + " не найден");
                    });

            int requestedQuantity = entry.getValue();
            if (item.getCount() < requestedQuantity) {
                log.warn("Insufficient stock for item {}. Available: {}, Requested: {}",
                        item.getTitle(), item.getCount(), requestedQuantity);
                throw new RuntimeException("Недостаточно товара '" + item.getTitle() +
                        "' на складе. Доступно: " + item.getCount() +
                        ", запрошено: " + requestedQuantity);
            }
        }
    }

    /**
     * Создание заказа и обновление остатков на складе с блокировкой
     */
    private Order createOrderWithLock(Map<Long, Integer> cartItems) {
        Order order = new Order();
        long totalSum = 0;

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            // Используем блокировку для обновления товара
            Item item = itemRepository.findByIdWithLock(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Товар с id " + entry.getKey() + " не найден"));

            int requestedQuantity = entry.getValue();
            int oldStock = item.getCount();
            int newCount = oldStock - requestedQuantity;

            if (newCount < 0) {
                log.error("Negative stock detected for item {}. Old: {}, Requested: {}",
                        item.getTitle(), oldStock, requestedQuantity);
                throw new RuntimeException("Недостаточно товара '" + item.getTitle() +
                        "' на складе. Доступно: " + oldStock +
                        ", запрошено: " + requestedQuantity);
            }

            item.setCount(newCount);
            itemRepository.save(item);

            OrderItem orderItem = new OrderItem(item, requestedQuantity);
            order.getItems().add(orderItem);
            totalSum += item.getPrice() * requestedQuantity;

            log.info("Updated stock: {} | Old: {} | Sold: {} | New: {}",
                    item.getTitle(), oldStock, requestedQuantity, newCount);
        }

        order.setTotalSum(totalSum);
        return orderService.saveOrder(order);
    }
}