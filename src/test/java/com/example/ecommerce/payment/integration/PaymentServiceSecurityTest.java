package com.example.ecommerce.payment.integration;

import com.example.ecommerce.payment.PaymentServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = PaymentServiceApplication.class
)
@ActiveProfiles("test")
public class PaymentServiceSecurityTest {

    @LocalServerPort
    private int port;

    @Test
    public void testUnauthorizedAccessToPaymentsEndpoint() {
        given()
            .port(port)
            .contentType("application/json")
            .when()
            .get("/api/payments/1")
            .then()
            .statusCode(401); // Unauthorized
    }

    @Test
    public void testPublicEndpointAccessible() {
        given()
            .port(port)
            .when()
            .get("/actuator/health")
            .then()
            .statusCode(200);
    }
}