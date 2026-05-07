package com.example.ecommerce.controller;

import com.example.ecommerce.dto.OrderDTO;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequestMapping("/orders")
@Tag(name = "Order", description = "Operations related to customer orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Get all orders", description = "Retrieves a list of all customer orders")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved orders")
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

    @Operation(summary = "Get order details", description = "Retrieves detailed information about a specific order by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order details")
    @ApiResponse(responseCode = "302", description = "Redirect to orders page if order not found")
    @GetMapping("/{id}")
    public Mono<String> getOrderDetails(@Parameter(description = "Order ID") @PathVariable Long id,
                                        @Parameter(description = "Flag indicating if this is a newly created order") @RequestParam(required = false) Boolean newOrder,
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