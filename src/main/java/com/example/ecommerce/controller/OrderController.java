package com.example.ecommerce.controller;

import com.example.ecommerce.dto.OrderDetailsDTO;
import com.example.ecommerce.dto.OrderSummaryDTO;
import com.example.ecommerce.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // GET /orders - список заказов (без items)
    @GetMapping({"", "/"})
    public String getOrders(Model model) {
        List<OrderSummaryDTO> orders = orderService.getAllOrderSummaries();
        model.addAttribute("orders", orders);
        return "orders";
    }

    // GET /orders/{id} - детали заказа (с items)
    @GetMapping("/{id}")
    public String getOrderDetails(@PathVariable Long id,
                                  @RequestParam(required = false) Boolean newOrder,
                                  Model model) {
        try {
            OrderDetailsDTO order = orderService.getOrderDetails(id);
            model.addAttribute("order", order);
            if (newOrder != null && newOrder) {
                model.addAttribute("newOrder", true);
            }
            return "order";
        } catch (RuntimeException e) {
            return "redirect:/orders";
        }
    }
}