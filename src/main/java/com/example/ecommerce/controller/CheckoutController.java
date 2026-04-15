package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.CheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

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