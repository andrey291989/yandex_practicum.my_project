package com.example.ecommerce.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-full")
@Testcontainers(disabledWithoutDocker = true)
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(java.time.Duration.ofMinutes(3))
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort()
                .withStartupTimeout(java.time.Duration.ofMinutes(2)));

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379)
            .withStartupTimeout(java.time.Duration.ofMinutes(3))
            .waitingFor(org.testcontainers.containers.wait.strategy.Wait.forListeningPort()
                .withStartupTimeout(java.time.Duration.ofMinutes(2)));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Настройки PostgreSQL
        registry.add("spring.datasource.url", () -> {
            if (postgres.isRunning()) {
                return postgres.getJdbcUrl();
            } else {
                return "jdbc:postgresql://localhost:5432/testdb";
            }
        });

        registry.add("spring.datasource.username", () -> {
            if (postgres.isRunning()) {
                return postgres.getUsername();
            } else {
                return "test";
            }
        });

        registry.add("spring.datasource.password", () -> {
            if (postgres.isRunning()) {
                return postgres.getPassword();
            } else {
                return "test";
            }
        });

        registry.add("spring.r2dbc.url", () -> {
            if (postgres.isRunning()) {
                return String.format("r2dbc:postgresql://%s:%d/%s",
                    postgres.getHost(),
                    postgres.getMappedPort(5432),
                    postgres.getDatabaseName());
            } else {
                return "r2dbc:postgresql://localhost:5432/testdb";
            }
        });

        registry.add("spring.r2dbc.username", () -> {
            if (postgres.isRunning()) {
                return postgres.getUsername();
            } else {
                return "test";
            }
        });

        registry.add("spring.r2dbc.password", () -> {
            if (postgres.isRunning()) {
                return postgres.getPassword();
            } else {
                return "test";
            }
        });

        // Настройки Redis
        registry.add("spring.redis.host", () -> {
            if (redis.isRunning()) {
                return redis.getHost();
            } else {
                return "localhost";
            }
        });

        registry.add("spring.redis.port", () -> {
            if (redis.isRunning()) {
                return String.valueOf(redis.getMappedPort(6379));
            } else {
                return "6379";
            }
        });

        // Flyway
        registry.add("spring.flyway.enabled", () -> "true");
    }
}