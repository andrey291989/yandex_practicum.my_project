package com.example.ecommerce.controller;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping({"", "/"})
    public Mono<String> getOrders(Model model) {
        log.info("GET /orders");

        return orderService.getAllOrders()
                .collectList()
                .doOnNext(orders -> log.info("Found {} orders", orders.size()))
                .flatMap(orders -> {
                    model.addAttribute("orders", orders);
                    return Mono.just("orders");
                });
    }

    @GetMapping("/{id}")
    public Mono<String> getOrderDetails(@PathVariable Long id,
                                        @RequestParam(required = false) Boolean newOrder,
                                        Model model) {
        log.info("GET /orders/{}", id);

        return orderService.getOrderById(id)
                .flatMap(order -> {
                    model.addAttribute("order", order);
                    if (newOrder != null && newOrder) {
                        model.addAttribute("newOrder", true);
                    }
                    return Mono.just("order");
                })
                .switchIfEmpty(Mono.just("redirect:/orders"));
    }
}