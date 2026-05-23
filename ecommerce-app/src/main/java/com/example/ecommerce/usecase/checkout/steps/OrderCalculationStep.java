package com.example.ecommerce.usecase.checkout.steps;

import com.example.ecommerce.service.ItemService;
import com.example.ecommerce.usecase.checkout.CheckoutContext;
import com.example.ecommerce.usecase.checkout.CheckoutStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Шаг расчета общей суммы заказа
 */
@Component
public class OrderCalculationStep implements CheckoutStep {

    private static final Logger log = LoggerFactory.getLogger(OrderCalculationStep.class);

    private final ItemService itemService;

    public OrderCalculationStep(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public Mono<CheckoutContext> execute(CheckoutContext context) {
        log.info("Executing order calculation step");

        return Flux.fromIterable(context.getCartItems().entrySet())
                .flatMap(entry -> {
                    Long itemId = entry.getKey();
                    Integer quantity = entry.getValue();

                    log.debug("Calculating price for item {} with quantity {}", itemId, quantity);
                    return itemService.getItemById(itemId)
                            .map(item -> item.getPrice() * quantity);
                })
                .reduce(0L, Long::sum)
                .doOnNext(totalSum -> log.info("Order total calculated: {}", totalSum))
                .map(totalSum -> {
                    context.setTotalSum(totalSum);
                    return context;
                });
    }
}