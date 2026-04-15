package com.example.ecommerce.controller;

import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(CartController.class)
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemService itemService;

    @Test
    void getCartItems_ShouldReturnCartPage() {
        when(cartService.getCartItems(any())).thenReturn(Mono.just(new HashMap<>()));

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateCartItem_WithPlusAction_ShouldAddToCart() {
        when(cartService.addToCart(any(), eq(1L))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "PLUS")
                        .queryParam("from", "cart")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void updateCartItem_WithMinusAction_ShouldDecrease() {
        when(cartService.decreaseQuantity(any(), eq(1L))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "MINUS")
                        .queryParam("from", "cart")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void updateCartItem_WithDeleteAction_ShouldRemove() {
        when(cartService.removeFromCart(any(), eq(1L))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/cart/items")
                        .queryParam("id", "1")
                        .queryParam("action", "DELETE")
                        .queryParam("from", "cart")
                        .build())
                .exchange()
                .expectStatus().is3xxRedirection();
    }
}