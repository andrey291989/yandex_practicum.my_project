package com.example.ecommerce.service;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.usecase.checkout.OrderCheckoutUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private OrderCheckoutUseCase orderCheckoutUseCase;

    private CheckoutService checkoutService;

    private WebSession session;

    @BeforeEach
    void setUp() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/")
        );
        session = exchange.getSession().block();

        checkoutService = new CheckoutService(orderCheckoutUseCase);
    }

    @Test
    void createOrderFromCart_ShouldDelegateToOrderCheckoutUseCase() {
        Map<Long, Integer> cartItems = new HashMap<>();
        cartItems.put(1L, 1);

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTotalSum(1000L);

        when(orderCheckoutUseCase.executeCheckout(session)).thenReturn(Mono.just(expectedOrder));

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void createOrderFromCart_WhenUseCaseThrowsException_ShouldPropagateException() {
        when(orderCheckoutUseCase.executeCheckout(session))
                .thenReturn(Mono.error(new RuntimeException("Ошибка оформления заказа")));

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Ошибка оформления заказа")
                )
                .verify();
    }
}