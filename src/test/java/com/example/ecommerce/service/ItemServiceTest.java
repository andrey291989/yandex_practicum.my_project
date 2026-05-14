package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.ReactiveListOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.anyList;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ReactiveRedisTemplate<String, Item> itemRedisTemplate;

    @Mock
    private ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate;

    @Mock
    private ReactiveValueOperations<String, Item> itemValueOperations;

    @Mock
    private ReactiveValueOperations<String, String> stringValueOperations;

    @Mock
    private ReactiveListOperations<String, Item> itemListOperations;

    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        // Створюємо сервіс з усіма моками
        itemService = new ItemService(itemRepository, itemRedisTemplate, stringReactiveRedisTemplate);

        testItem = new Item();
        testItem.setId(1L);
        testItem.setTitle("Тестовый товар");
        testItem.setDescription("Описание");
        testItem.setPrice(1000L);
        testItem.setCount(50);
        testItem.setImgPath("test.jpg");
    }

    private void mockRedisValueOperations() {
        lenient().when(itemRedisTemplate.opsForValue()).thenReturn(itemValueOperations);
        lenient().when(stringReactiveRedisTemplate.opsForValue()).thenReturn(stringValueOperations);
    }

    private void mockRedisListOperations() {
        lenient().when(itemRedisTemplate.opsForList()).thenReturn(itemListOperations);
    }

    @Test
    void getItemById_WhenExistsInCache_ShouldReturnItemFromCache() {
        mockRedisValueOperations();
        when(itemValueOperations.get("item:1")).thenReturn(Mono.just(testItem));
        lenient().when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));

        StepVerifier.create(itemService.getItemById(1L))
                .expectNextMatches(item -> item.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getItemById_WhenNotInCache_ShouldReturnItemFromDB() {
        mockRedisValueOperations();
        when(itemValueOperations.get("item:1")).thenReturn(Mono.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.just(testItem));
        when(itemValueOperations.set(anyString(), any(Item.class), any(Duration.class))).thenReturn(Mono.just(true));

        StepVerifier.create(itemService.getItemById(1L))
                .expectNextMatches(item -> item.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void getItemById_WhenNotExists_ShouldReturnEmpty() {
        mockRedisValueOperations();
        when(itemValueOperations.get("item:99")).thenReturn(Mono.empty());
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
        mockRedisValueOperations();
        lenient().when(itemRedisTemplate.delete("item:1")).thenAnswer(invocation -> Mono.just(1L));
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

    @Test
    void getItemsPage_WhenExistsInCache_ShouldReturnItemsFromCache() {
        mockRedisListOperations();
        when(itemListOperations.range("items:page:all:ALPHA:1:10", 0, -1)).thenReturn(Flux.just(testItem));

        StepVerifier.create(itemService.getItemsPage(null, "ALPHA", 1, 10).collectList())
                .expectNextMatches(items -> items.size() == 1)
                .verifyComplete();
    }

    @Test
    void getItemsPage_WhenNotInCache_ShouldReturnItemsFromDB() {
        mockRedisListOperations();
        mockRedisValueOperations();
        lenient().when(itemRedisTemplate.delete(anyString())).thenAnswer(invocation -> Mono.just(1L));
        when(itemListOperations.range("items:page:all:ALPHA:1:10", 0, -1)).thenReturn(Flux.empty());
        when(itemRepository.findAllSortedByTitleAsc(10, 0)).thenReturn(Flux.just(testItem));
        when(itemListOperations.leftPushAll(anyString(), anyList())).thenAnswer(invocation -> Mono.just(1L));
        when(itemRedisTemplate.expire(anyString(), any(Duration.class))).thenAnswer(invocation -> Mono.just(true));

        StepVerifier.create(itemService.getItemsPage(null, "ALPHA", 1, 10).collectList())
                .expectNextMatches(items -> items.size() == 1)
                .verifyComplete();
    }

    @Test
    void getTotalCount_WhenExistsInCache_ShouldReturnCountFromCache() {
        mockRedisValueOperations();
        when(stringValueOperations.get("items:count:all")).thenReturn(Mono.just("5"));

        StepVerifier.create(itemService.getTotalCount(null))
                .expectNext(5L)
                .verifyComplete();
    }

    @Test
    void getTotalCount_WhenNotInCache_ShouldReturnCountFromDB() {
        mockRedisValueOperations();
        lenient().when(stringReactiveRedisTemplate.delete(anyString())).thenAnswer(invocation -> Mono.just(1L));
        when(stringValueOperations.get("items:count:all")).thenReturn(Mono.empty());
        when(itemRepository.count()).thenReturn(Mono.just(5L));
        when(stringValueOperations.set(anyString(), anyString())).thenAnswer(invocation -> Mono.just(true));
        when(stringReactiveRedisTemplate.expire(anyString(), any(Duration.class))).thenAnswer(invocation -> Mono.just(true));

        StepVerifier.create(itemService.getTotalCount(null))
                .expectNext(5L)
                .verifyComplete();
    }
}