package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ItemDTO;
import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/cart")
@Tag(name = "Cart", description = "Operations related to shopping cart management")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final ItemService itemService;

    public CartController(CartService cartService, ItemService itemService) {
        this.cartService = cartService;
        this.itemService = itemService;
    }

    @Operation(summary = "Get cart items", description = "Retrieves all items currently in the shopping cart")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved cart items")
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

    @Operation(summary = "Add item to cart", description = "Adds a specific item to the shopping cart")
    @ApiResponse(responseCode = "302", description = "Redirect to cart items page")
    @PostMapping("/items/{id}")
    public Mono<String> addToCart(@Parameter(description = "Item ID to add to cart") @PathVariable Long id, WebSession session) {
        log.info("POST /cart/items/{} - add to cart", id);
        return cartService.addToCart(session, id)
                .then(Mono.just("redirect:/cart/items"));
    }

    @Operation(summary = "Update cart item", description = "Updates a cart item based on the specified action (PLUS, MINUS, DELETE)")
    @ApiResponse(responseCode = "302", description = "Redirect to appropriate page")
    @PostMapping("/items")
    public Mono<String> updateCartItem(@Parameter(description = "Item ID to update") @RequestParam(required = false) Long id,
                                       @Parameter(description = "Action to perform (PLUS, MINUS, DELETE)") @RequestParam(required = false) String action,
                                       @Parameter(description = "Source page for redirect") @RequestParam(required = false, defaultValue = "catalog") String from,
                                       WebSession session,
                                       ServerHttpRequest request) {
        log.info("POST /cart/items - id: {}, action: {}, from: {}", id, action, from);

        // Log request details for debugging
        log.info("Request headers: {}", request.getHeaders());
        log.info("Request URI: {}", request.getURI());

        // Handle case when parameters are not passed as @RequestParam but in form data
        if (id == null || action == null) {
            log.warn("Parameters not received as @RequestParam, trying alternative approach");
            // In this case, we'll redirect to prevent errors
            return Mono.just(createRedirectUrl(from));
        }

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

    /**
     * Alternative method to handle form data submission for adding items to cart
     * This method specifically handles the case where form data is submitted via POST
     */
    @PostMapping("/items/add")
    public Mono<String> addItemToCart(ServerWebExchange exchange,
                                      @Parameter(description = "Source page for redirect") @RequestParam(required = false, defaultValue = "catalog") String from,
                                      WebSession session) {
        log.info("POST /cart/items/add - from: {}", from);

        // Extract form data manually
        return exchange.getFormData()
                .flatMap(formData -> {
                    String idStr = formData.getFirst("id");
                    if (idStr == null || idStr.isEmpty()) {
                        log.warn("Item ID is missing in form data");
                        return Mono.just(createRedirectUrl(from));
                    }

                    try {
                        Long id = Long.valueOf(idStr);
                        log.info("Adding item {} to cart", id);
                        return cartService.addToCart(session, id)
                                .then(Mono.just(createRedirectUrl(from)));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid item ID format: {}", idStr);
                        return Mono.just(createRedirectUrl(from));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error processing form data", e);
                    return Mono.just(createRedirectUrl(from));
                });
    }

    @Operation(summary = "Update item quantity", description = "Increases or decreases the quantity of a specific item in the cart")
    @ApiResponse(responseCode = "302", description = "Redirect to cart items page")
    @PatchMapping("/items/{id}")
    public Mono<String> updateQuantity(@Parameter(description = "Item ID to update") @PathVariable Long id,
                                       @Parameter(description = "Action to perform (INCREASE, DECREASE)") @RequestParam String action,
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

    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the shopping cart")
    @ApiResponse(responseCode = "302", description = "Redirect to cart items page")
    @DeleteMapping("/items/{id}")
    public Mono<String> removeFromCart(@Parameter(description = "Item ID to remove from cart") @PathVariable Long id, WebSession session) {
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