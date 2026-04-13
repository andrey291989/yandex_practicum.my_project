package com.example.ecommerce.integration;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EcommerceApplicationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void contextLoads() {
        assertThat(restTemplate).isNotNull();
    }

    @Test
    void homePage_ShouldReturnOk() {
        // Изменено: ожидаем 200 OK, так как корневой путь теперь маппится на ItemController.redirectToItems
        ResponseEntity<String> response = restTemplate.getForEntity("/", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void itemsPage_ShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/items", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Витрина магазина");
    }

    @Test
    void cartPage_ShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/cart/items", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Корзина");
    }

    @Test
    void ordersPage_ShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.getForEntity("/orders", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void itemDetailsPage_ShouldReturnOk() {
        // Arrange
        Item item = new Item();
        item.setTitle("Тестовый товар");
        item.setPrice(1000L);
        item.setCount(10);
        Item saved = itemRepository.save(item);

        // Act
        ResponseEntity<String> response = restTemplate.getForEntity("/items/" + saved.getId(), String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}