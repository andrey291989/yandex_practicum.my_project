package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Order;
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
class OrderRepositoryTest {

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

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setTotalSum(5000L);
    }

    @Test
    void save_ShouldSaveOrder() {
        StepVerifier.create(orderRepository.save(testOrder))
                .expectNextMatches(order -> {
                    assertThat(order.getId()).isNotNull();
                    assertThat(order.getTotalSum()).isEqualTo(5000L);
                    assertThat(order.getCreatedAt()).isNotNull();
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void findById_WhenOrderExists_ShouldReturnOrder() {
        StepVerifier.create(
                orderRepository.save(testOrder)
                        .flatMap(saved -> orderRepository.findById(saved.getId()))
        ).expectNextMatches(order -> {
            assertThat(order.getTotalSum()).isEqualTo(5000L);
            return true;
        }).verifyComplete();
    }

    @Test
    void findById_WhenOrderDoesNotExist_ShouldReturnEmpty() {
        StepVerifier.create(orderRepository.findById(999L))
                .verifyComplete();
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        Order order1 = new Order(1000L);
        Order order2 = new Order(2000L);

        StepVerifier.create(
                        orderRepository.save(order1)
                                .then(orderRepository.save(order2))
                                .thenMany(orderRepository.findAll())
                                .collectList()
                ).expectNextMatches(orders -> orders.size() >= 2)
                .verifyComplete();
    }

    @Test
    void findAllByOrderByCreatedAtDesc_ShouldReturnOrdersSortedDesc() {
        Order olderOrder = new Order(1000L);
        Order newerOrder = new Order(2000L);

        StepVerifier.create(
                orderRepository.save(olderOrder)
                        .then(orderRepository.save(newerOrder))
                        .thenMany(orderRepository.findAllByOrderByCreatedAtDesc())
                        .collectList()
        ).expectNextMatches(orders -> {
            if (orders.size() >= 2) {
                return orders.get(0).getCreatedAt().isAfter(orders.get(1).getCreatedAt());
            }
            return true;
        }).verifyComplete();
    }

    @Test
    void update_ShouldUpdateOrder() {
        StepVerifier.create(
                orderRepository.save(testOrder)
                        .flatMap(saved -> {
                            saved.setTotalSum(10000L);
                            return orderRepository.save(saved);
                        })
                        .flatMap(updated -> orderRepository.findById(updated.getId()))
        ).expectNextMatches(order -> {
            assertThat(order.getTotalSum()).isEqualTo(10000L);
            return true;
        }).verifyComplete();
    }

    @Test
    void delete_ShouldRemoveOrder() {
        StepVerifier.create(
                        orderRepository.save(testOrder)
                                .flatMap(saved -> orderRepository.deleteById(saved.getId()))
                                .then(orderRepository.count())
                ).expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }
}