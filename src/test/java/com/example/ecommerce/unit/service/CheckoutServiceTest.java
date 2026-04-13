package com.example.ecommerce.unit.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.repository.ItemRepository;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.CheckoutService;
import com.example.ecommerce.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    private MockHttpSession session;
    private Item testItem;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("Тестовый товар");
        testItem.setPrice(1000L);
        testItem.setCount(50);
    }

    @Test
    void createOrderFromCart_WhenStockSufficient_ShouldCreateOrder() {
        // Arrange
        when(cartService.getCartItems(session)).thenReturn(Map.of(1L, 1));
        // Используем findByIdWithLock вместо findById
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));

        Order expectedOrder = new Order();
        expectedOrder.setId(1L);
        expectedOrder.setTotalSum(1000L);
        when(orderService.saveOrder(any(Order.class))).thenReturn(expectedOrder);

        doNothing().when(cartService).clearCart(session);

        // Act
        Order result = checkoutService.createOrderFromCart(session);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTotalSum()).isEqualTo(1000L);
        verify(itemRepository, times(1)).save(any(Item.class));
        verify(orderService, times(1)).saveOrder(any(Order.class));
        verify(cartService, times(1)).clearCart(session);
    }

    @Test
    void createOrderFromCart_WhenCartEmpty_ShouldThrowException() {
        // Arrange
        when(cartService.getCartItems(session)).thenReturn(Map.of());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.createOrderFromCart(session))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Корзина пуста");
    }

    @Test
    void createOrderFromCart_WhenItemNotFound_ShouldThrowException() {
        // Arrange
        when(cartService.getCartItems(session)).thenReturn(Map.of(99L, 1));
        when(itemRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.createOrderFromCart(session))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Товар с id 99 не найден");
    }

    @Test
    void createOrderFromCart_WhenStockInsufficient_ShouldThrowException() {
        // Arrange
        when(cartService.getCartItems(session)).thenReturn(Map.of(1L, 100));
        testItem.setCount(50);
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));

        // Act & Assert
        assertThatThrownBy(() -> checkoutService.createOrderFromCart(session))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Недостаточно товара");
    }
}