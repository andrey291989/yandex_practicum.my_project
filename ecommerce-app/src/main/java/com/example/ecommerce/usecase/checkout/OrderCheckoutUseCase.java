package com.example.ecommerce.usecase.checkout;

import com.example.ecommerce.cache.ItemCacheService;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.usecase.checkout.steps.CartValidationStep;
import com.example.ecommerce.usecase.checkout.steps.OrderCalculationStep;
import com.example.ecommerce.usecase.checkout.steps.OrderCreationStep;
import com.example.ecommerce.usecase.checkout.steps.OrderItemsCreationStep;
import com.example.ecommerce.usecase.checkout.steps.StockValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Цельный прикладной сценарий оформления заказа
 * Представляет собой последовательность шагов, выполняемых как единый бизнес-процесс
 */
@Service
public class OrderCheckoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderCheckoutUseCase.class);

    private final CartService cartService;
    private final ItemCacheService itemCacheService;
    private final TransactionalOperator transactionalOperator;

    // Шаги оформления заказа
    private final CartValidationStep cartValidationStep;
    private final StockValidationStep stockValidationStep;
    private final OrderCalculationStep orderCalculationStep;
    private final OrderCreationStep orderCreationStep;
    private final OrderItemsCreationStep orderItemsCreationStep;

    public OrderCheckoutUseCase(CartService cartService,
                                ItemCacheService itemCacheService,
                                CartValidationStep cartValidationStep,
                                StockValidationStep stockValidationStep,
                                OrderCalculationStep orderCalculationStep,
                                OrderCreationStep orderCreationStep,
                                OrderItemsCreationStep orderItemsCreationStep,
                                TransactionalOperator transactionalOperator) {
        this.cartService = cartService;
        this.itemCacheService = itemCacheService;
        this.cartValidationStep = cartValidationStep;
        this.stockValidationStep = stockValidationStep;
        this.orderCalculationStep = orderCalculationStep;
        this.orderCreationStep = orderCreationStep;
        this.orderItemsCreationStep = orderItemsCreationStep;
        this.transactionalOperator = transactionalOperator;
    }

    /**
     * Выполнить цельный сценарий оформления заказа
     *
     * @param session веб-сессия пользователя
     * @return Mono с созданным заказом
     */
    public Mono<Order> executeCheckout(WebSession session) {
        log.info("Starting order checkout process for session: {}", session.getId());

        return cartService.getCartItems(session)
                .flatMap(cartItems -> {
                    CheckoutContext context = new CheckoutContext(cartItems);

                    return executeSteps(context)
                            .flatMap(resultContext -> cartService.clearCart(session)
                                    .thenReturn(resultContext.getOrder()))
                            .doOnSuccess(order -> {
                                cartItems.keySet().forEach(itemCacheService::invalidateItemCache);
                                itemCacheService.invalidateListCaches();
                            });
                })
                .as(transactionalOperator::transactional)
                .doOnSuccess(order -> log.info("Order checkout completed successfully. Order ID: {}", order.getId()))
                .doOnError(error -> log.error("Order checkout failed: {}", error.getMessage()));
    }

    /**
     * Выполнить последовательность шагов оформления заказа
     */
    private Mono<CheckoutContext> executeSteps(CheckoutContext context) {
        List<CheckoutStep> steps = List.of(
                cartValidationStep,
                stockValidationStep,
                orderCalculationStep,
                orderCreationStep,
                orderItemsCreationStep
        );

        return Mono.fromCallable(() -> {
                    log.info("Starting checkout steps execution");
                    return context;
                })
                .flatMap(executeStepChain(steps, 0));
    }

    /**
     * Рекурсивно выполнить цепочку шагов
     */
    private java.util.function.Function<CheckoutContext, Mono<CheckoutContext>> executeStepChain(
            List<CheckoutStep> steps, int index) {

        if (index >= steps.size()) {
            return Mono::just; // Все шаги выполнены
        }

        CheckoutStep currentStep = steps.get(index);
        String stepName = currentStep.getClass().getSimpleName();

        return context -> {
            log.info("Executing step {}: {}", index + 1, stepName);
            return currentStep.execute(context)
                    .flatMap(nextContext -> {
                        log.info("Step {} completed successfully", stepName);
                        // Переход к следующему шагу
                        return executeStepChain(steps, index + 1).apply(nextContext);
                    })
                    .onErrorResume(error -> {
                        log.error("Step {} failed: {}", stepName, error.getMessage());
                        return Mono.error(new RuntimeException(
                                "Ошибка на шаге " + stepName + ": " + error.getMessage(), error));
                    });
        };
    }
}