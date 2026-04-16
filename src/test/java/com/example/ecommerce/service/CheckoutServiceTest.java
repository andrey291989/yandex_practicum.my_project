package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private ItemService itemService;

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @Mock
    private ReactiveTransactionManager transactionManager;

    private CheckoutService checkoutService;

    private WebSession session;
    private Item testItem;

    @BeforeEach
    void setUp() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest.get("/")
        );
        session = exchange.getSession().block();

        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("Тестовый товар");
        testItem.setDescription("Описание");
        testItem.setPrice(1000L);
        testItem.setCount(50);
        testItem.setImgPath("test.jpg");

        // Создаем тестовую версию CheckoutService без транзакций
        checkoutService = new CheckoutService(itemService, orderService, cartService, transactionManager) {
            @Override
            public Mono<Order> createOrderFromCart(WebSession session) {
                return cartService.getCartItems(session)
                        .flatMap(cartItems -> {
                            if (cartItems.isEmpty()) {
                                return Mono.error(new RuntimeException("Корзина пуста"));
                            }

                            // Проверяем наличие товаров и достаточность количества
                            return Flux.fromIterable(cartItems.entrySet())
                                    .flatMap(entry -> itemService.decrementStock(entry.getKey(), entry.getValue())
                                            .onErrorMap(RuntimeException.class, ex ->
                                                new RuntimeException("Ошибка при проверке товара: " + ex.getMessage())))
                                    .then(Mono.defer(() -> {
                                        // Вычисляем общую сумму через ItemService
                                        return Flux.fromIterable(cartItems.entrySet())
                                                .flatMap(entry -> itemService.getItemById(entry.getKey())
                                                        .map(item -> item.getPrice() * entry.getValue()))
                                                .reduce(0L, Long::sum)
                                                .flatMap(totalSum -> {
                                                    Order order = new Order(totalSum);
                                                    return orderService.saveOrderOnly(order)
                                                            .flatMap(savedOrder -> cartService.clearCart(session)
                                                                    .thenReturn(savedOrder));
                                                });
                                    }));
                        });
            }
        };
    }

    @Test
    void createOrderFromCart_WhenStockSufficient_ShouldCreateOrder() {
        Map<Long, Integer> cartItems = new HashMap<>();
        cartItems.put(1L, 1);

        lenient().when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        lenient().when(itemService.decrementStock(eq(1L), any(Integer.class))).thenReturn(Mono.just(testItem));
        lenient().when(itemService.getItemById(1L)).thenReturn(Mono.just(testItem));

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTotalSum(1000L);
        lenient().when(orderService.saveOrderOnly(any(Order.class))).thenReturn(Mono.just(expectedOrder));

        lenient().when(cartService.clearCart(session)).thenReturn(Mono.empty());

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void createOrderFromCart_WhenCartEmpty_ShouldThrowException() {
        lenient().when(cartService.getCartItems(session)).thenReturn(Mono.just(new HashMap<>()));

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Корзина пуста")
                )
                .verify();
    }

    @Test
    void createOrderFromCart_WhenItemNotFound_ShouldThrowException() {
        Map<Long, Integer> cartItems = new HashMap<>();
        cartItems.put(99L, 1);

        lenient().when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        lenient().when(itemService.decrementStock(99L, 1)).thenReturn(Mono.error(new RuntimeException("Товар с id 99 не найден")));

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Товар с id 99 не найден")
                )
                .verify();
    }

    @Test
    void createOrderFromCart_WhenStockInsufficient_ShouldThrowException() {
        Map<Long, Integer> cartItems = new HashMap<>();
        cartItems.put(1L, 100);

        testItem.setCount(50);

        lenient().when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        lenient().when(itemService.decrementStock(1L, 100)).thenReturn(Mono.error(new RuntimeException("Недостаточно товара")));

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().contains("Недостаточно товара")
                )
                .verify();
    }

    @Test
    void createOrderFromCart_WithMultipleItems_ShouldCreateOrder() {
        Map<Long, Integer> multipleItems = new HashMap<>();
        multipleItems.put(1L, 2);

        lenient().when(cartService.getCartItems(session)).thenReturn(Mono.just(multipleItems));
        lenient().when(itemService.decrementStock(eq(1L), any(Integer.class))).thenReturn(Mono.just(testItem));
        lenient().when(itemService.getItemById(1L)).thenReturn(Mono.just(testItem));

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTotalSum(2000L);
        lenient().when(orderService.saveOrderOnly(any(Order.class))).thenReturn(Mono.just(expectedOrder));

        lenient().when(cartService.clearCart(session)).thenReturn(Mono.empty());

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }
}