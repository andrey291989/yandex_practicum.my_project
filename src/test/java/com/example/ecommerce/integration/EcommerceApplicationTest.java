package com.example.ecommerce.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EcommerceIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void homePage_ShouldRedirectToItems() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/items");
    }

    @Test
    void itemsPage_ShouldReturnOk() {
        webTestClient.get()
                .uri("/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void cartPage_ShouldReturnOk() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void ordersPage_ShouldReturnOk() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }
}