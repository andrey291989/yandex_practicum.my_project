package com.example.ecommerce.usecase.checkout;

import com.example.ecommerce.cache.ItemCacheService;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.usecase.checkout.steps.CartValidationStep;
import com.example.ecommerce.usecase.checkout.steps.OrderCalculationStep;
import com.example.ecommerce.usecase.checkout.steps.OrderCreationStep;
import com.example.ecommerce.usecase.checkout.steps.OrderItemsCreationStep;
import com.example.ecommerce.usecase.checkout.steps.StockValidationStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderCheckoutUseCaseTest {

    @Mock
    private CartService cartService;

    @Mock
    private CartValidationStep cartValidationStep;

    @Mock
    private StockValidationStep stockValidationStep;

    @Mock
    private OrderCalculationStep orderCalculationStep;

    @Mock
    private OrderCreationStep orderCreationStep;

    @Mock
    private OrderItemsCreationStep orderItemsCreationStep;

    @Mock
    private ItemCacheService itemCacheService;

    @Mock
    private TransactionalOperator transactionalOperator;

    private OrderCheckoutUseCase orderCheckoutUseCase;

    private WebSession session;

    @BeforeEach
    void setUp() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/")
        );
        session = exchange.getSession().block();

        // Прокидываем оригинальный поток «как есть», без транзакционной обёртки
        when(transactionalOperator.transactional((Mono<Object>) any())).thenAnswer(inv -> inv.getArgument(0));

        orderCheckoutUseCase = new OrderCheckoutUseCase(
                cartService,
                itemCacheService,
                cartValidationStep,
                stockValidationStep,
                orderCalculationStep,
                orderCreationStep,
                orderItemsCreationStep,
                transactionalOperator
        );
    }

    @Test
    void executeCheckout_WhenSuccessful_ShouldReturnOrder() {
        Map<Long, Integer> cartItems = new HashMap<>();
        cartItems.put(1L, 1);

        CheckoutContext context = new CheckoutContext(cartItems);
        context.setTotalSum(1000L);

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTotalSum(1000L);
        context.setOrder(expectedOrder);

        when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        when(cartValidationStep.execute(any(CheckoutContext.class))).thenReturn(Mono.just(context));
        when(stockValidationStep.execute(any(CheckoutContext.class))).thenReturn(Mono.just(context));
        when(orderCalculationStep.execute(any(CheckoutContext.class))).thenReturn(Mono.just(context));
        when(orderCreationStep.execute(any(CheckoutContext.class))).thenReturn(Mono.just(context));
        when(orderItemsCreationStep.execute(any(CheckoutContext.class))).thenReturn(Mono.just(context));
        when(cartService.clearCart(session)).thenReturn(Mono.empty());

        StepVerifier.create(orderCheckoutUseCase.executeCheckout(session))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void executeCheckout_WhenCartEmpty_ShouldThrowException() {
        when(cartService.getCartItems(session)).thenReturn(Mono.just(new HashMap<>()));

        StepVerifier.create(orderCheckoutUseCase.executeCheckout(session))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void executeCheckout_WhenStepFails_ShouldPropagateException() {
        Map<Long, Integer> cartItems = new HashMap<>();
        cartItems.put(1L, 1);

        when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        when(cartValidationStep.execute(any(CheckoutContext.class))).thenReturn(Mono.just(new CheckoutContext(cartItems)));
        when(stockValidationStep.execute(any(CheckoutContext.class)))
                .thenReturn(Mono.error(new RuntimeException("Stock validation failed")));

        StepVerifier.create(orderCheckoutUseCase.executeCheckout(session))
                .expectError(RuntimeException.class)
                .verify();
    }
}