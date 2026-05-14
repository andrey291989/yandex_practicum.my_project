package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RedisGracefulDegradationService redisDegradationService;

    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository, redisDegradationService);

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
        when(redisDegradationService.getCachedItemWithFallback(eq("item:1"), any()))
            .thenAnswer(invocation -> {
                Supplier<Mono<Item>> fallback = invocation.getArgument(1);
                return fallback.get();
            });
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(itemService.getItemById(1L))
                .expectNextMatches(item -> item.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getItemById_WhenNotExists_ShouldReturnEmpty() {
        when(redisDegradationService.getCachedItemWithFallback(eq("item:99"), any()))
            .thenAnswer(invocation -> {
                Supplier<Mono<Item>> fallback = invocation.getArgument(1);
                return fallback.get();
            });
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
        when(redisDegradationService.invalidateCache("item:1")).thenReturn(Mono.empty());
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