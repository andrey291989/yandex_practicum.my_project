package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, Item> itemRedisTemplate;
    private final ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate;

    public ItemService(ItemRepository itemRepository,
                       ReactiveRedisTemplate<String, Item> itemRedisTemplate,
                       ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate) {
        this.itemRepository = itemRepository;
        this.itemRedisTemplate = itemRedisTemplate;
        this.stringReactiveRedisTemplate = stringReactiveRedisTemplate;
    }

    public Flux<Item> getItemsPage(String search, String sort, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        int limit = pageSize;

        // Генерация ключа для кэша
        String cacheKey = String.format("items:page:%s:%s:%d:%d",
            search != null ? search : "all",
            sort != null ? sort : "ALPHA",
            pageNumber, pageSize);

        // Попытка получить из кэша
        return itemRedisTemplate.opsForList().range(cacheKey, 0, -1)
            .doOnNext(item -> log.debug("Items page found in cache for key: {}", cacheKey))
            .switchIfEmpty(
                // Если нет в кэше, получаем из БД
                Flux.defer(() -> {
                    if (search != null && !search.trim().isEmpty()) {
                        log.debug("Searching for: {} with pagination", search);
                        return itemRepository.searchItemsWithPagination(search, limit, offset);
                    }

                    if ("ALPHA".equalsIgnoreCase(sort)) {
                        log.debug("Sorting by title ascending with pagination");
                        return itemRepository.findAllSortedByTitleAsc(limit, offset);
                    } else if ("PRICE_ASC".equalsIgnoreCase(sort)) {
                        log.debug("Sorting by price ascending with pagination");
                        return itemRepository.findAllSortedByPriceAsc(limit, offset);
                    } else if ("PRICE_DESC".equalsIgnoreCase(sort)) {
                        log.debug("Sorting by price descending with pagination");
                        return itemRepository.findAllSortedByPriceDesc(limit, offset);
                    } else {
                        log.debug("No sorting applied, using default pagination");
                        return itemRepository.findAllSortedByTitleAsc(limit, offset);
                    }
                })
                .collectList()
                .flatMapMany(items -> {
                    if (items.isEmpty()) {
                        return Flux.empty();
                    }

                    // Сохраняем в кэш
                    return itemRedisTemplate.opsForList().leftPushAll(cacheKey, items)
                        .then(itemRedisTemplate.expire(cacheKey, CACHE_TTL))
                        .thenMany(Flux.fromIterable(items))
                        .doOnComplete(() -> log.debug("Items page saved to cache with key: {}", cacheKey));
                })
            );
    }

    public Mono<Long> getTotalCount(String search) {
        // Генерация ключа для кэша
        String cacheKey = "items:count:" + (search != null ? search : "all");

        // Попытка получить из кэша
        return stringReactiveRedisTemplate.opsForValue().get(cacheKey)
            .flatMap(value -> {
                try {
                    Long count = Long.parseLong(value);
                    return Mono.just(count);
                } catch (NumberFormatException e) {
                    return Mono.empty();
                }
            })
            .doOnNext(count -> log.debug("Items count found in cache for key: {}", cacheKey))
            .switchIfEmpty(
                // Если нет в кэше, получаем из БД
                Mono.defer(() -> {
                    if (search != null && !search.trim().isEmpty()) {
                        return itemRepository.countBySearch(search);
                    }
                    return itemRepository.count();
                })
                .doOnNext(count -> log.debug("Items count fetched from database for key: {}", cacheKey))
                .flatMap(count ->
                    // Сохраняем в кэш
                    stringReactiveRedisTemplate.opsForValue().set(cacheKey, count.toString())
                        .then(stringReactiveRedisTemplate.expire(cacheKey, CACHE_TTL))
                        .then(Mono.just(count))
                        .doOnSuccess(saved -> log.debug("Items count saved to cache with key: {}", cacheKey))
                )
            );
    }

    public Mono<Item> getItemById(Long id) {
        log.debug("Fetching item by id: {}", id);

        String key = "item:" + id;

        // Попытка получить из кэша
        return itemRedisTemplate.opsForValue().get(key)
            .doOnNext(item -> log.debug("Item {} found in cache", id))
            .switchIfEmpty(
                // Если нет в кэше, получаем из БД
                itemRepository.findById(id)
                    .doOnNext(item -> log.debug("Item {} fetched from database", id))
                    .flatMap(item ->
                        // Сохраняем в кэш
                        itemRedisTemplate.opsForValue().set(key, item, CACHE_TTL)
                            .then(Mono.just(item))
                            .doOnSuccess(saved -> log.debug("Item {} saved to cache", id))
                    )
            );
    }

    public Flux<Item> getAllItems() {
        log.debug("Fetching all items");
        return itemRepository.findAll();
    }

    public Mono<Item> updateItem(Item item) {
        log.info("Updating item: id={}, title={}", item.getId(), item.getTitle());

        // Инвалидация кэша перед обновлением
        String key = "item:" + item.getId();

        // Также инвалидируем кэши списков, так как обновление может повлиять на результаты поиска/пагинации
        return itemRedisTemplate.delete(key)
            .doOnNext(deleted -> log.debug("Cache invalidated for item {}", item.getId()))
            .then(invalidateListCaches())
            .then(itemRepository.save(item))
            .doOnSuccess(updatedItem -> log.info("Item {} updated successfully", item.getId()));
    }

    /**
     * Инвалидация всех кэшей списков (для случаев, когда изменения могут повлиять на результаты поиска/пагинации)
     */
    private Mono<Void> invalidateListCaches() {
        // В реальном приложении здесь можно было бы использовать шаблон ключей или теги
        // Для простоты пропустим эту часть, так как в Reactive Redis нет прямого аналога keys()
        // В продакшене лучше использовать более точечную инвалидацию через теги или префиксы
        log.debug("List cache invalidation skipped in reactive mode");
        return Mono.empty();
    }

    public Mono<Boolean> checkStockAvailability(Long itemId, int requestedQuantity) {
        return itemRepository.findById(itemId)
                .map(item -> {
                    int stock = item.getCount() != null ? item.getCount() : 0;
                    boolean available = stock >= requestedQuantity;
                    if (!available) {
                        log.debug("Insufficient stock for item {}. Available: {}, Requested: {}",
                                item.getTitle(), stock, requestedQuantity);
                    }
                    return available;
                })
                .defaultIfEmpty(false);
    }

    public Mono<Integer> getAvailableStock(Long itemId) {
        return itemRepository.findById(itemId)
                .map(item -> item.getCount() != null ? item.getCount() : 0)
                .defaultIfEmpty(0);
    }

    /**
     * Уменьшает количество товара на складе
     * @param itemId ID товара
     * @param quantity количество для списания
     * @return Mono с обновленным товаром или ошибкой, если недостаточно товара
     */
    public Mono<Item> decrementStock(Long itemId, int quantity) {
        return itemRepository.findByIdWithLock(itemId)
                .switchIfEmpty(Mono.error(new RuntimeException("Товар с id " + itemId + " не найден")))
                .flatMap(item -> {
                    int currentStock = item.getCount() != null ? item.getCount() : 0;
                    int newStock = currentStock - quantity;

                    if (newStock < 0) {
                        return Mono.error(new RuntimeException("Недостаточно товара '" + item.getTitle() +
                                "' на складе. Доступно: " + currentStock +
                                ", запрошено: " + quantity));
                    }

                    item.setCount(newStock);
                    log.info("Updated stock for item {}: {} -> {}", itemId, currentStock, newStock);

                    // Инвалидация кэша после обновления
                    String key = "item:" + itemId;
                    return itemRedisTemplate.delete(key)
                        .doOnNext(deleted -> log.debug("Cache invalidated for item {}", itemId))
                        .then(itemRepository.save(item));
                });
    }
}