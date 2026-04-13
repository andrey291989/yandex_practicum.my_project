package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setTitle("Тестовый товар");
        testItem.setDescription("Описание тестового товара");
        testItem.setPrice(1000L);
        testItem.setCount(50);
        testItem.setImgPath("test.jpg");
        itemRepository.save(testItem);
    }

    @Test
    void searchItems_WithSearchTerm_ShouldReturnMatchingItems() {
        // Act
        Page<Item> result = itemRepository.searchItems("тестовый", PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getContent().get(0).getTitle()).contains("Тестовый");
    }

    @Test
    void searchItems_WithEmptySearch_ShouldReturnAllItems() {
        // Act
        Page<Item> result = itemRepository.searchItems(null, PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isNotEmpty();
    }

    @Test
    void searchItems_WithNonMatchingSearch_ShouldReturnEmptyPage() {
        // Act
        Page<Item> result = itemRepository.searchItems("несуществующийтовар", PageRequest.of(0, 10));

        // Assert
        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findById_ShouldReturnItem() {
        // Act
        Item result = itemRepository.findById(testItem.getId()).orElse(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    void save_ShouldPersistItem() {
        // Arrange
        Item newItem = new Item();
        newItem.setTitle("Новый товар");
        newItem.setPrice(2000L);
        newItem.setCount(10);

        // Act
        Item saved = itemRepository.save(newItem);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Новый товар");
    }
}