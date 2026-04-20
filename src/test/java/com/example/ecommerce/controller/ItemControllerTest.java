package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @Test
    void getItems_ShouldReturnItemsPage() {
        when(itemService.getItemsPage(any(), any(), anyInt(), anyInt())).thenReturn(Flux.empty());
        when(itemService.getTotalCount(any())).thenReturn(Mono.just(0L));
        when(cartService.getCartItems(any())).thenReturn(Mono.just(new HashMap<>()));

        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItemDetails_WhenExists_ShouldReturnItemPage() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");

        when(itemService.getItemById(1L)).thenReturn(Mono.just(item));
        when(cartService.getCartItems(any())).thenReturn(Mono.just(new HashMap<>()));

        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getItemDetails_WhenNotExists_ShouldRedirect() {
        when(itemService.getItemById(99L)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/items/99")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void redirectToItems_ShouldRedirect() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items");
    }
}