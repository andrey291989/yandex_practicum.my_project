package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    private ItemRepository itemRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private CartService cartService;

    @InjectMocks
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
    }

    @Test
    void createOrderFromCart_WhenStockSufficient_ShouldCreateOrder() {
        Map<Long, Integer> cartItems = new HashMap<>();
        cartItems.put(1L, 1);

        when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(testItem));

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTotalSum(1000L);
        when(orderService.saveOrderOnly(any(Order.class))).thenReturn(Mono.just(expectedOrder));

        when(cartService.clearCart(session)).thenReturn(Mono.empty());

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void createOrderFromCart_WhenCartEmpty_ShouldThrowException() {
        when(cartService.getCartItems(session)).thenReturn(Mono.just(new HashMap<>()));

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

        when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        when(itemRepository.findById(99L)).thenReturn(Mono.empty());

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

        when(cartService.getCartItems(session)).thenReturn(Mono.just(cartItems));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

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

        when(cartService.getCartItems(session)).thenReturn(Mono.just(multipleItems));
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(Mono.just(testItem));

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTotalSum(2000L);
        when(orderService.saveOrderOnly(any(Order.class))).thenReturn(Mono.just(expectedOrder));

        when(cartService.clearCart(session)).thenReturn(Mono.empty());

        StepVerifier.create(checkoutService.createOrderFromCart(session))
                .expectNextMatches(order -> order.getId().equals(1L))
                .verifyComplete();
    }
}