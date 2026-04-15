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

import java.util.HashMap;
import java.util.Map;

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

    @PostMapping("/items")
    public Mono<String> updateCartItem(
            WebSession session,
            @RequestParam Long id,
            @RequestParam String action,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize) {

        log.info("POST /cart/items - id: {}, action: {}, from: {}", id, action, from);

        Mono<Void> operation;
        switch (action) {
            case "PLUS":
                operation = cartService.addToCart(session, id);
                break;
            case "MINUS":
                operation = cartService.decreaseQuantity(session, id);
                break;
            case "DELETE":
                operation = cartService.removeFromCart(session, id);
                break;
            default:
                operation = Mono.empty();
        }

        return operation.then(Mono.defer(() -> {
            if ("cart".equals(from)) {
                return Mono.just("redirect:/cart/items");
            } else {
                StringBuilder redirect = new StringBuilder("redirect:/items");
                boolean hasParams = false;

                if (search != null && !search.isEmpty()) {
                    redirect.append("?search=").append(search);
                    hasParams = true;
                }
                if (sort != null && !"ALPHA".equals(sort)) {
                    redirect.append(hasParams ? "&" : "?").append("sort=").append(sort);
                    hasParams = true;
                }
                if (pageSize != 10) {
                    redirect.append(hasParams ? "&" : "?").append("pageSize=").append(pageSize);
                    hasParams = true;
                }
                if (pageNumber != 1) {
                    redirect.append(hasParams ? "&" : "?").append("pageNumber=").append(pageNumber);
                }

                return Mono.just(redirect.toString());
            }
        }));
    }
}