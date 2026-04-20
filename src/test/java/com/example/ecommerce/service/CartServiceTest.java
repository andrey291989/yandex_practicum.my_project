package com.example.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CartServiceTest {

    private CartService cartService;
    private WebSession session;

    @BeforeEach
    void setUp() {
        cartService = new CartService();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/")
        );
        session = exchange.getSession().block();
    }

    @Test
    void addToCart_ShouldAddItem() {
        StepVerifier.create(
                cartService.addToCart(session, 1L)
                        .then(cartService.getCartItems(session))
        ).assertNext(cart -> {
            assertThat(cart).containsKey(1L);
            assertThat(cart.get(1L)).isEqualTo(1);
        }).verifyComplete();
    }

    @Test
    void addToCart_Twice_ShouldIncreaseQuantity() {
        StepVerifier.create(
                cartService.addToCart(session, 1L)
                        .then(cartService.addToCart(session, 1L))
                        .then(cartService.getCartItems(session))
        ).assertNext(cart -> {
            assertThat(cart.get(1L)).isEqualTo(2);
        }).verifyComplete();
    }

    @Test
    void removeFromCart_ShouldRemoveItem() {
        StepVerifier.create(
                cartService.addToCart(session, 1L)
                        .then(cartService.removeFromCart(session, 1L))
                        .then(cartService.getCartItems(session))
        ).assertNext(cart -> {
            assertThat(cart).doesNotContainKey(1L);
        }).verifyComplete();
    }

    @Test
    void decreaseQuantity_ShouldDecrease() {
        StepVerifier.create(
                cartService.addToCart(session, 1L)
                        .then(cartService.addToCart(session, 1L))
                        .then(cartService.decreaseQuantity(session, 1L))
                        .then(cartService.getCartItems(session))
        ).assertNext(cart -> {
            assertThat(cart.get(1L)).isEqualTo(1);
        }).verifyComplete();
    }

    @Test
    void clearCart_ShouldEmptyCart() {
        StepVerifier.create(
                cartService.addToCart(session, 1L)
                        .then(cartService.addToCart(session, 2L))
                        .then(cartService.clearCart(session))
                        .then(cartService.getCartItems(session))
        ).assertNext(cart -> {
            assertThat(cart).isEmpty();
        }).verifyComplete();
    }
}