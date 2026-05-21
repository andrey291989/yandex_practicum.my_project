package com.example.ecommerce.integration;

import com.example.ecommerce.EcommerceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = EcommerceApplication.class
)
@Testcontainers
@ActiveProfiles("test")
public class SecurityIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ecommerce_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
            "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/ecommerce_test");
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("spring.datasource.url", () ->
            "jdbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/ecommerce_test");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Test
    public void testPublicAccessToItemsPage() {
        given()
            .port(port)
            .when()
            .get("/items")
            .then()
            .statusCode(200)
            .body(containsString("Витрина магазина"));
    }

    @Test
    public void testUnauthorizedAccessToCartPage() {
        given()
            .port(port)
            .when()
            .get("/cart/items")
            .then()
            .statusCode(302); // Redirect to login page
    }

    @Test
    public void testUnauthorizedAccessToOrdersPage() {
        given()
            .port(port)
            .when()
            .get("/orders")
            .then()
            .statusCode(302); // Redirect to login page
    }

    @Test
    public void testLoginPageAccessible() {
        given()
            .port(port)
            .when()
            .get("/login")
            .then()
            .statusCode(200)
            .body(containsString("Вход в систему"));
    }
}