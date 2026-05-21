package com.example.ecommerce.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WebClientConfigTest {

    @MockBean
    private ReactiveOAuth2AuthorizedClientManager authorizedClientManager;

    @Test
    void testWebClientBuilderCreation() {
        WebClientConfig config = new WebClientConfig();

        WebClient.Builder builder = config.webClientBuilder(authorizedClientManager);

        assertThat(builder).isNotNull();
    }
}