package com.example.ecommerce.usecase.checkout;

import reactor.core.publisher.Mono;

/**
 * Интерфейс шага оформления заказа
 */
public interface CheckoutStep {

    /**
     * Выполнить шаг оформления заказа
     * @param context контекст оформления заказа
     * @return_mono с обновленным контекстом
     */
    Mono<CheckoutContext> execute(CheckoutContext context);
}