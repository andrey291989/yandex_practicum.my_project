package com.example.ecommerce.usecase.checkout.steps;

import com.example.ecommerce.usecase.checkout.CheckoutContext;
import com.example.ecommerce.usecase.checkout.CheckoutStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Шаг валидации корзины
 */
@Component
public class CartValidationStep implements CheckoutStep {

    private static final Logger log = LoggerFactory.getLogger(CartValidationStep.class);

    @Override
    public Mono<CheckoutContext> execute(CheckoutContext context) {
        log.info("Executing cart validation step");

        if (context.getCartItems().isEmpty()) {
            log.warn("Cart is empty");
            return Mono.error(new RuntimeException("Корзина пуста"));
        }

        log.info("Cart validation successful. Items in cart: {}", context.getCartItems().size());
        return Mono.just(context);
    }
}