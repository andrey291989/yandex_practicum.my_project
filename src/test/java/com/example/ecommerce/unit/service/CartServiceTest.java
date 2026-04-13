package com.example.ecommerce.unit.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import com.example.ecommerce.service.CartService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

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
    void addToCart_ShouldAddItemToCart() {
        // Act
        cartService.addToCart(session, 1L);

        // Assert
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        assertThat(cartItems).containsEntry(1L, 1);
    }

    @Test
    void addToCart_ExistingItem_ShouldIncreaseQuantity() {
        // Arrange
        cartService.addToCart(session, 1L);

        // Act
        cartService.addToCart(session, 1L);

        // Assert
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        assertThat(cartItems).containsEntry(1L, 2);
    }

    @Test
    void removeFromCart_ShouldRemoveItem() {
        // Arrange
        cartService.addToCart(session, 1L);
        cartService.addToCart(session, 1L);

        // Act
        cartService.removeFromCart(session, 1L);

        // Assert
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        assertThat(cartItems).doesNotContainKey(1L);
    }

    @Test
    void decreaseQuantity_ShouldDecreaseQuantity() {
        // Arrange
        cartService.addToCart(session, 1L);
        cartService.addToCart(session, 1L);

        // Act
        cartService.decreaseQuantity(session, 1L);

        // Assert
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        assertThat(cartItems).containsEntry(1L, 1);
    }

    @Test
    void decreaseQuantity_WhenQuantityIsOne_ShouldRemoveItem() {
        // Arrange
        cartService.addToCart(session, 1L);

        // Act
        cartService.decreaseQuantity(session, 1L);

        // Assert
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        assertThat(cartItems).doesNotContainKey(1L);
    }

    @Test
    void getCartItems_ShouldReturnCopyOfCart() {
        // Arrange
        cartService.addToCart(session, 1L);
        cartService.addToCart(session, 2L);

        // Act
        Map<Long, Integer> cartItems = cartService.getCartItems(session);

        // Assert
        assertThat(cartItems).hasSize(2);
        assertThat(cartItems).containsEntry(1L, 1);
        assertThat(cartItems).containsEntry(2L, 1);
    }

    @Test
    void clearCart_ShouldEmptyCart() {
        // Arrange
        cartService.addToCart(session, 1L);
        cartService.addToCart(session, 2L);

        // Act
        cartService.clearCart(session);

        // Assert
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        assertThat(cartItems).isEmpty();
    }

    @Test
    void checkStockAvailability_WhenStockSufficient_ShouldReturnTrue() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        boolean result = cartService.checkStockAvailability(1L, 10);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void checkStockAvailability_WhenStockInsufficient_ShouldReturnFalse() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        boolean result = cartService.checkStockAvailability(1L, 100);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void getAvailableStock_ShouldReturnStockCount() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        int result = cartService.getAvailableStock(1L);

        // Assert
        assertThat(result).isEqualTo(50);
    }

    @Test
    void getAvailableStock_WhenItemNotFound_ShouldReturnZero() {
        // Arrange
        when(itemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        int result = cartService.getAvailableStock(99L);

        // Assert
        assertThat(result).isEqualTo(0);
    }
}