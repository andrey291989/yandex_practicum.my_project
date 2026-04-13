package com.example.ecommerce.unit.controller;

import com.example.ecommerce.controller.ItemController;
import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @MockBean
    private CartService cartService;

    @Test
    void getItems_ShouldReturnItemsPage() throws Exception {
        // Arrange
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");
        item.setPrice(1000L);
        item.setCount(10);

        Page<Item> page = new PageImpl<>(Arrays.asList(item), PageRequest.of(0, 10), 1);
        when(itemService.getItemsPage(any(), any(), anyInt(), anyInt())).thenReturn(page);
        when(cartService.getCartItems(any(HttpSession.class))).thenReturn(new HashMap<>());

        // Act & Assert
        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    void getItems_WithSearchParam_ShouldPassSearchToService() throws Exception {
        // Arrange
        Page<Item> page = new PageImpl<>(Arrays.asList(), PageRequest.of(0, 10), 0);
        when(itemService.getItemsPage(eq("тест"), any(), anyInt(), anyInt())).thenReturn(page);
        when(cartService.getCartItems(any(HttpSession.class))).thenReturn(new HashMap<>());

        // Act & Assert
        mockMvc.perform(get("/items").param("search", "тест"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"));
    }

    @Test
    void getItemDetails_WhenItemExists_ShouldReturnItemPage() throws Exception {
        // Arrange
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Тестовый товар");
        item.setPrice(1000L);
        item.setCount(10);
        when(itemService.getItemById(1L)).thenReturn(item);
        when(cartService.getCartItems(any(HttpSession.class))).thenReturn(new HashMap<>());

        // Act & Assert
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void getItemDetails_WhenItemDoesNotExist_ShouldRedirectToItems() throws Exception {
        // Arrange
        when(itemService.getItemById(99L)).thenReturn(null);

        // Act & Assert
        mockMvc.perform(get("/items/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
    }

    @Test
    void redirectToItems_ShouldRedirect() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items"));
    }
}