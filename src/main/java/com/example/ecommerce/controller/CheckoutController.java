package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.CheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/buy")
    public String buy(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Order order = checkoutService.createOrderFromCart(session);
            redirectAttributes.addFlashAttribute("success", "Заказ успешно оформлен!");
            log.info("Order created successfully: orderId={}", order.getId());
            return "redirect:/orders/" + order.getId() + "?newOrder=true";
        } catch (RuntimeException e) {
            log.error("Failed to create order: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart/items";
        }
    }
}