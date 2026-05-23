package com.example.ecommerce.service;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.usecase.checkout.OrderCheckoutUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

/**
 * Сервис оформления заказа
 * Делегирует выполнение бизнес-процесса OrderCheckoutUseCase
 */
@Service
public class CheckoutService {

    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    private final OrderCheckoutUseCase orderCheckoutUseCase;

    public CheckoutService(OrderCheckoutUseCase orderCheckoutUseCase) {
        this.orderCheckoutUseCase = orderCheckoutUseCase;
    }

    /**
     * Создать заказ из корзины пользователя
     * @param session веб-сессия пользователя
     * @return_mono с созданным заказом
     */
    public Mono<Order> createOrderFromCart(WebSession session) {
        log.info("Initiating order creation from cart for session: {}", session.getId());
        return orderCheckoutUseCase.executeCheckout(session);
    }
}