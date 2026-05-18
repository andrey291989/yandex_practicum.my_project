package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@Tag(name = "Checkout", description = "Operations related to order checkout process")
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Operation(summary = "Create order from cart", description = "Creates a new order from the current cart contents and clears the cart")
    @ApiResponse(responseCode = "302", description = "Redirect to order details page on success, or cart page on error")
    @PostMapping("/buy")
    public Mono<String> buy(WebSession session) {
        log.info("POST /buy - Session: {}", session.getId());

        return checkoutService.createOrderFromCart(session)
                .doOnNext(order -> log.info("Order created successfully: orderId={}", order.getId()))
                .flatMap(order -> Mono.just("redirect:/orders/" + order.getId() + "?newOrder=true"))
                .onErrorResume(e -> {
                    log.error("Failed to create order: {}", e.getMessage());
                    return Mono.just("redirect:/cart/items?error=" + e.getMessage());
                });
    }
}