package com.example.ecommerce;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.example.ecommerce.integration.AbstractIntegrationTest;

import java.time.Duration;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration-full")
@Tag("integration-full")
class EcommerceApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
    }
}