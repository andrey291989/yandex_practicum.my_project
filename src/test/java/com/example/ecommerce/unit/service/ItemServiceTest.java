package com.example.ecommerce.unit.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import com.example.ecommerce.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("Тестовый товар");
        testItem.setDescription("Описание тестового товара");
        testItem.setPrice(1000L);
        testItem.setCount(50);
        testItem.setImgPath("test.jpg");
    }

    @Test
    void getItemsPage_WithNoSort_ShouldReturnUnsortedPage() {
        // Arrange
        Page<Item> expectedPage = new PageImpl<>(Arrays.asList(testItem));
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(itemRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<Item> result = itemService.getItemsPage(null, "NO", 1, 10);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(itemRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getItemsPage_WithAlphaSort_ShouldReturnSortedPage() {
        // Arrange
        Page<Item> expectedPage = new PageImpl<>(Arrays.asList(testItem));
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("title").ascending());
        when(itemRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<Item> result = itemService.getItemsPage(null, "ALPHA", 1, 10);

        // Assert
        assertThat(result).isNotNull();
        verify(itemRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getItemsPage_WithPriceSort_ShouldReturnPriceSortedPage() {
        // Arrange
        Page<Item> expectedPage = new PageImpl<>(Arrays.asList(testItem));
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("price").ascending());
        when(itemRepository.findAll(pageRequest)).thenReturn(expectedPage);

        // Act
        Page<Item> result = itemService.getItemsPage(null, "PRICE", 1, 10);

        // Assert
        assertThat(result).isNotNull();
        verify(itemRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void getItemsPage_WithSearch_ShouldCallSearchItems() {
        // Arrange
        String searchTerm = "тест";
        Page<Item> expectedPage = new PageImpl<>(Arrays.asList(testItem));
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(itemRepository.searchItems(eq(searchTerm), eq(pageRequest))).thenReturn(expectedPage);

        // Act
        Page<Item> result = itemService.getItemsPage(searchTerm, "NO", 1, 10);

        // Assert
        assertThat(result).isNotNull();
        verify(itemRepository, times(1)).searchItems(eq(searchTerm), any(PageRequest.class));
    }

    @Test
    void getItemById_WhenItemExists_ShouldReturnItem() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        Item result = itemService.getItemById(1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Тестовый товар");
    }

    @Test
    void getItemById_WhenItemDoesNotExist_ShouldReturnNull() {
        // Arrange
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Item result = itemService.getItemById(99L);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void getAllItems_ShouldReturnAllItems() {
        // Arrange
        List<Item> expectedItems = Arrays.asList(testItem);
        when(itemRepository.findAll()).thenReturn(expectedItems);

        // Act
        List<Item> result = itemService.getAllItems();

        // Assert
        assertThat(result).hasSize(1);
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void updateItem_ShouldSaveItem() {
        // Arrange
        when(itemRepository.save(testItem)).thenReturn(testItem);

        // Act
        itemService.updateItem(testItem);

        // Assert
        verify(itemRepository, times(1)).save(testItem);
    }
}