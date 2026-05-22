package com.example.ecommerce.usecase.checkout.steps;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.ItemService;
import com.example.ecommerce.usecase.checkout.CheckoutContext;
import com.example.ecommerce.usecase.checkout.CheckoutStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 * Шаг проверки наличия товаров и списания остатков
 */
@Component
public class StockValidationStep implements CheckoutStep {

    private static final Logger log = LoggerFactory.getLogger(StockValidationStep.class);

    private final ItemService itemService;

    public StockValidationStep(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public Mono<CheckoutContext> execute(CheckoutContext context) {
        log.info("Executing stock validation step");

        return Flux.fromIterable(context.getCartItems().entrySet())
                .flatMap(entry -> {
                    Long itemId = entry.getKey();
                    Integer quantity = entry.getValue();

                    log.debug("Checking stock for item {} with quantity {}", itemId, quantity);
                    return itemService.decrementStock(itemId, quantity)
                            .onErrorMap(RuntimeException.class, ex -> {
                                log.error("Stock validation failed for item {}: {}", itemId, ex.getMessage());
                                return new RuntimeException("Ошибка при проверке товара " + itemId + ": " + ex.getMessage());
                            });
                })
                .collectList()
                .doOnNext(items -> log.info("Stock validation completed for {} items", items.size()))
                .map(validatedItems -> {
                    context.setValidatedItems(new ArrayList<>(validatedItems));
                    return context;
                });
    }
}