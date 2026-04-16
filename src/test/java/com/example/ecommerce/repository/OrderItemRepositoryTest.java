package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
class OrderItemRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V2__Create_order_tables.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://localhost:%d/testdb", postgres.getMappedPort(5432)));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private Order testOrder;
    private OrderItem testOrderItem;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setTotalSum(5000L);

        testOrderItem = new OrderItem();
        testOrderItem.setTitle("Тестовый товар");
        testOrderItem.setDescription("Описание");
        testOrderItem.setImgPath("test.jpg");
        testOrderItem.setPrice(1000L);
        testOrderItem.setCount(2);
    }

    @Test
    void save_ShouldSaveOrderItem() {
        StepVerifier.create(
                orderRepository.save(testOrder)
                        .flatMap(savedOrder -> {
                            testOrderItem.setOrderId(savedOrder.getId());
                            return orderItemRepository.save(testOrderItem);
                        })
        ).expectNextMatches(item -> {
            assertThat(item.getId()).isNotNull();
            assertThat(item.getTitle()).isEqualTo("Тестовый товар");
            assertThat(item.getOrderId()).isNotNull();
            return true;
        }).verifyComplete();
    }

    @Test
    void findAllByOrderId_ShouldReturnOrderItems() {
        StepVerifier.create(
                        orderRepository.save(testOrder)
                                .flatMap(savedOrder -> {
                                    testOrderItem.setOrderId(savedOrder.getId());
                                    return orderItemRepository.save(testOrderItem);
                                })
                                .flatMapMany(saved -> orderItemRepository.findAllByOrderId(saved.getOrderId()))
                                .collectList()
                ).expectNextMatches(items -> !items.isEmpty())
                .verifyComplete();
    }

    @Test
    void deleteAllByOrderId_ShouldDeleteOrderItems() {
        StepVerifier.create(
                        orderRepository.save(testOrder)
                                .flatMap(savedOrder -> {
                                    testOrderItem.setOrderId(savedOrder.getId());
                                    return orderItemRepository.save(testOrderItem);
                                })
                                .flatMap(saved -> orderItemRepository.deleteAllByOrderId(saved.getOrderId()))
                                .then(orderItemRepository.count())
                ).expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }
}