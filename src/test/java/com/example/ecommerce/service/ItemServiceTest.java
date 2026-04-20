package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        testItem.setDescription("Описание");
        testItem.setPrice(1000L);
        testItem.setCount(50);
        testItem.setImgPath("test.jpg");
    }

    @Test
    void getItemById_WhenExists_ShouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(itemService.getItemById(1L))
                .expectNextMatches(item -> item.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getItemById_WhenNotExists_ShouldReturnEmpty() {
        when(itemRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(itemService.getItemById(99L))
                .verifyComplete();
    }

    @Test
    void getAllItems_ShouldReturnFluxOfItems() {
        when(itemRepository.findAll()).thenReturn(Flux.just(testItem));

        StepVerifier.create(itemService.getAllItems().collectList())
                .expectNextMatches(items -> items.size() == 1)
                .verifyComplete();
    }

    @Test
    void updateItem_ShouldSaveAndReturnItem() {
        when(itemRepository.save(testItem)).thenReturn(Mono.just(testItem));

        StepVerifier.create(itemService.updateItem(testItem))
                .expectNextMatches(item -> item.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void checkStockAvailability_WhenSufficient_ShouldReturnTrue() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(itemService.checkStockAvailability(1L, 10))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void checkStockAvailability_WhenInsufficient_ShouldReturnFalse() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(itemService.checkStockAvailability(1L, 100))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void getAvailableStock_ShouldReturnStockCount() {
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(itemService.getAvailableStock(1L))
                .expectNext(50)
                .verifyComplete();
    }
}