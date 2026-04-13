package com.example.ecommerce.unit.controller;

import com.example.ecommerce.controller.CartController;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemService itemService;

    @Test
    void getCartItems_ShouldReturnCartPage() throws Exception {
        // Arrange
        when(cartService.getCartItems(any(HttpSession.class))).thenReturn(new HashMap<>());

        // Act & Assert
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"));
    }

    @Test
    void updateCartItem_WithPlusAction_ShouldAddToCart() throws Exception {
        // Arrange
        when(cartService.checkStockAvailability(anyLong(), anyInt())).thenReturn(true);
        doNothing().when(cartService).addToCart(any(HttpSession.class), anyLong());

        // Act & Assert
        // Исправлено: ожидаем редирект без pageSize, так как pageSize=10 (значение по умолчанию)
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("pageSize", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        verify(cartService, times(1)).addToCart(any(HttpSession.class), eq(1L));
    }

    @Test
    void updateCartItem_WithMinusAction_ShouldDecreaseQuantity() throws Exception {
        // Arrange
        doNothing().when(cartService).decreaseQuantity(any(HttpSession.class), anyLong());

        // Act & Assert
        // Исправлено: ожидаем редирект без pageSize, так как pageSize=10 (значение по умолчанию)
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "MINUS")
                        .param("pageSize", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));

        verify(cartService, times(1)).decreaseQuantity(any(HttpSession.class), eq(1L));
    }

    @Test
    void updateCartItem_WithPlusAction_WhenPageSizeNotDefault_ShouldRedirectWithPageSize() throws Exception {
        // Arrange
        when(cartService.checkStockAvailability(anyLong(), anyInt())).thenReturn(true);
        doNothing().when(cartService).addToCart(any(HttpSession.class), anyLong());

        // Act & Assert
        // Когда pageSize не равен значению по умолчанию (10), параметр должен быть в URL
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS")
                        .param("pageSize", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?pageSize=20"));

        verify(cartService, times(1)).addToCart(any(HttpSession.class), eq(1L));
    }

    @Test
    void updateCartItem_WithDeleteAction_ShouldRemoveFromCart() throws Exception {
        // Arrange
        doNothing().when(cartService).removeFromCart(any(HttpSession.class), anyLong());

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "DELETE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        verify(cartService, times(1)).removeFromCart(any(HttpSession.class), eq(1L));
    }

    @Test
    void updateCartItem_WithInsufficientStock_ShouldReturnError() throws Exception {
        // Arrange
        when(cartService.checkStockAvailability(anyLong(), anyInt())).thenReturn(false);
        when(cartService.getAvailableStock(anyLong())).thenReturn(0);

        // Act & Assert
        mockMvc.perform(post("/cart/items")
                        .param("id", "1")
                        .param("action", "PLUS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"))
                .andExpect(flash().attributeExists("error"));
    }
}