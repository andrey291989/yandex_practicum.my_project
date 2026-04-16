package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ItemDTO;
import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final ItemService itemService;

    public CartController(CartService cartService, ItemService itemService) {
        this.cartService = cartService;
        this.itemService = itemService;
    }

    @GetMapping("/items")
    public Mono<String> getCartItems(WebSession session, Model model) {
        log.info("GET /cart/items");

        return cartService.getCartItems(session)
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        model.addAttribute("items", java.util.Collections.emptyList());
                        model.addAttribute("total", 0L);
                        return Mono.just("cart");
                    }

                    return Flux.fromIterable(cartItems.entrySet())
                            .flatMap(entry -> itemService.getItemById(entry.getKey())
                                    .map(item -> ItemDTO.fromEntity(item, entry.getValue())))
                            .collectList()
                            .flatMap(items -> {
                                long total = items.stream()
                                        .mapToLong(item -> item.price() * item.cartCount())
                                        .sum();

                                model.addAttribute("items", items);
                                model.addAttribute("total", total);
                                return Mono.just("cart");
                            });
                });
    }

    @PostMapping("/items/{id}")
    public Mono<String> addToCart(@PathVariable Long id, WebSession session) {
        log.info("POST /cart/items/{} - add to cart", id);
        return cartService.addToCart(session, id)
                .then(Mono.just("redirect:/cart/items"));
    }

    @PostMapping("/items")
    public Mono<String> updateCartItem(@RequestParam Long id,
                                       @RequestParam String action,
                                       @RequestParam(required = false, defaultValue = "catalog") String from,
                                       WebSession session) {
        log.info("POST /cart/items - id: {}, action: {}, from: {}", id, action, from);

        switch (action.toUpperCase()) {
            case "PLUS":
                return cartService.addToCart(session, id)
                        .then(Mono.just(createRedirectUrl(from)));
            case "MINUS":
                return cartService.decreaseQuantity(session, id)
                        .then(Mono.just(createRedirectUrl(from)));
            case "DELETE":
                return cartService.removeFromCart(session, id)
                        .then(Mono.just(createRedirectUrl(from)));
            default:
                return Mono.just(createRedirectUrl(from));
        }
    }

    @PatchMapping("/items/{id}")
    public Mono<String> updateQuantity(@PathVariable Long id,
                                       @RequestParam String action,
                                       WebSession session) {
        log.info("PATCH /cart/items/{} - action: {}", id, action);

        if ("INCREASE".equalsIgnoreCase(action)) {
            return cartService.addToCart(session, id)
                    .then(Mono.just("redirect:/cart/items"));
        } else if ("DECREASE".equalsIgnoreCase(action)) {
            return cartService.decreaseQuantity(session, id)
                    .then(Mono.just("redirect:/cart/items"));
        }
        return Mono.just("redirect:/cart/items");
    }

    @DeleteMapping("/items/{id}")
    public Mono<String> removeFromCart(@PathVariable Long id, WebSession session) {
        log.info("DELETE /cart/items/{} - remove from cart", id);
        return cartService.removeFromCart(session, id)
                .then(Mono.just("redirect:/cart/items"));
    }

    /**
     * Создает правильный redirect URL в зависимости от источника
     * @param from источник (catalog, cart, item и т.д.)
     * @return redirect URL
     */
    private String createRedirectUrl(String from) {
        if (from == null || from.isEmpty()) {
            return "redirect:/";
        }

        // Нормализуем путь - убираем начальный слеш если есть, добавляем если нужно
        String normalizedFrom = from.startsWith("/") ? from.substring(1) : from;

        // Для основных страниц используем корневой путь
        switch (normalizedFrom.toLowerCase()) {
            case "catalog":
            case "items":
                return "redirect:/items";
            case "cart":
                return "redirect:/cart/items";
            case "home":
            case "":
                return "redirect:/";
            default:
                // Для остальных случаев используем как есть, но с префиксом redirect:/
                return "redirect:/" + normalizedFrom;
        }
    }
}