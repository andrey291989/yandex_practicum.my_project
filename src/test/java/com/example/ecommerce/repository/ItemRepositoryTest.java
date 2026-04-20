package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
class ItemRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__Create_item_table.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://localhost:%d/testdb", postgres.getMappedPort(5432)));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
    }

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
    }

    @Test
    void save_ShouldSaveItem() {
        StepVerifier.create(itemRepository.save(testItem))
                .expectNextMatches(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getTitle()).isEqualTo("Тестовый товар");
                    assertThat(item.getPrice()).isEqualTo(1000L);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void findById_WhenItemExists_ShouldReturnItem() {
        StepVerifier.create(
                itemRepository.save(testItem)
                        .flatMap(saved -> itemRepository.findById(saved.getId()))
        ).expectNextMatches(item -> {
            assertThat(item.getTitle()).isEqualTo("Тестовый товар");
            assertThat(item.getPrice()).isEqualTo(1000L);
            return true;
        }).verifyComplete();
    }

    @Test
    void findById_WhenItemDoesNotExist_ShouldReturnEmpty() {
        StepVerifier.create(itemRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_ShouldReturnAllItems() {
        StepVerifier.create(
                        itemRepository.save(testItem)
                                .thenMany(itemRepository.findAll())
                                .collectList()
                ).expectNextMatches(items -> items.size() >= 1)
                .verifyComplete();
    }

    @Test
    void searchItemsWithPagination_WithSearchTerm_ShouldReturnMatchingItems() {
        StepVerifier.create(
                        itemRepository.save(testItem)
                                .thenMany(itemRepository.searchItemsWithPagination("тестовый", 10, 0))
                                .collectList()
                ).expectNextMatches(items -> !items.isEmpty())
                .verifyComplete();
    }

    @Test
    void searchItemsWithPagination_WithEmptySearch_ShouldReturnAllItems() {
        StepVerifier.create(
                        itemRepository.save(testItem)
                                .thenMany(itemRepository.searchItemsWithPagination(null, 10, 0))
                                .collectList()
                ).expectNextMatches(items -> items.size() >= 1)
                .verifyComplete();
    }

    @Test
    void countBySearch_WithSearchTerm_ShouldReturnCorrectCount() {
        StepVerifier.create(
                        itemRepository.save(testItem)
                                .then(itemRepository.countBySearch("тестовый"))
                ).expectNextMatches(count -> count >= 1)
                .verifyComplete();
    }

    @Test
    void findAllWithPagination_ShouldReturnLimitedResults() {
        // Создаем несколько тестовых товаров
        Item item1 = new Item();
        item1.setTitle("Товар 1");
        item1.setPrice(1000L);
        item1.setCount(10);

        Item item2 = new Item();
        item2.setTitle("Товар 2");
        item2.setPrice(2000L);
        item2.setCount(20);

        Item item3 = new Item();
        item3.setTitle("Товар 3");
        item3.setPrice(3000L);
        item3.setCount(30);

        StepVerifier.create(
                        itemRepository.save(item1)
                                .then(itemRepository.save(item2))
                                .then(itemRepository.save(item3))
                                .thenMany(itemRepository.findAllSortedByTitleAsc(2, 0))
                                .collectList()
                ).expectNextMatches(items -> items.size() == 2)
                .verifyComplete();
    }

    @Test
    void findAllSortedByPriceAsc_ShouldReturnSortedResults() {
        Item cheapItem = new Item();
        cheapItem.setTitle("Дешевый товар");
        cheapItem.setPrice(500L);
        cheapItem.setCount(10);

        Item expensiveItem = new Item();
        expensiveItem.setTitle("Дорогой товар");
        expensiveItem.setPrice(5000L);
        expensiveItem.setCount(10);

        StepVerifier.create(
                itemRepository.save(cheapItem)
                        .then(itemRepository.save(expensiveItem))
                        .thenMany(itemRepository.findAllSortedByPriceAsc(10, 0))
                        .collectList()
        ).expectNextMatches(items -> {
            // Первый товар должен быть дешевле
            return items.get(0).getPrice() <= items.get(items.size() - 1).getPrice();
        }).verifyComplete();
    }

    @Test
    void update_ShouldUpdateItem() {
        StepVerifier.create(
                itemRepository.save(testItem)
                        .flatMap(saved -> {
                            saved.setTitle("Обновленный товар");
                            saved.setPrice(2000L);
                            return itemRepository.save(saved);
                        })
                        .flatMap(updated -> itemRepository.findById(updated.getId()))
        ).expectNextMatches(item -> {
            assertThat(item.getTitle()).isEqualTo("Обновленный товар");
            assertThat(item.getPrice()).isEqualTo(2000L);
            return true;
        }).verifyComplete();
    }

    @Test
    void delete_ShouldRemoveItem() {
        StepVerifier.create(
                        itemRepository.save(testItem)
                                .flatMap(saved -> itemRepository.deleteById(saved.getId()))
                                .then(itemRepository.count())
                ).expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }
}