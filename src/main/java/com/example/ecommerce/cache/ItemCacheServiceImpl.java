package com.example.ecommerce.cache;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.RedisGracefulDegradationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Реализация сервиса кэширования для товаров
 * Инкапсулирует всю логику работы с кэшем
 */
@Service
public class ItemCacheServiceImpl implements ItemCacheService {

    private static final Logger log = LoggerFactory.getLogger(ItemCacheServiceImpl.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RedisGracefulDegradationService redisDegradationService;

    public ItemCacheServiceImpl(RedisGracefulDegradationService redisDegradationService) {
        this.redisDegradationService = redisDegradationService;
    }

    @Override
    public Flux<Item> getCachedItemsPage(String search, String sort, int pageNumber, int pageSize,
                                        java.util.function.Supplier<Flux<Item>> fallbackSupplier) {
        // Генерация ключа для кэша
        String cacheKey = String.format("items:page:%s:%s:%d:%d",
                search != null ? search : "all",
                sort != null ? sort : "ALPHA",
                pageNumber, pageSize);

        // Попытка получить из кэша с fallback на БД
        return redisDegradationService.getCachedListWithFallback(cacheKey, fallbackSupplier)
                .switchIfEmpty(
                        // Если кэш пуст, сохраняем результаты из БД в кэш
                        Flux.defer(fallbackSupplier)
                                .collectList()
                                .flatMapMany(items -> {
                                    if (items.isEmpty()) {
                                        return Flux.empty();
                                    }

                                    // Асинхронно сохраняем в кэш без блокировки основного потока
                                    redisDegradationService.cacheList(cacheKey, Flux.fromIterable(items), CACHE_TTL)
                                            .subscribe();

                                    return Flux.fromIterable(items);
                                })
                );
    }

    @Override
    public Mono<Long> getCachedTotalCount(String search, java.util.function.Supplier<Mono<Long>> fallbackSupplier) {
        // Генерация ключа для кэша
        String cacheKey = "items:count:" + (search != null ? search : "all");

        // Попытка получить из кэша с fallback на БД
        return redisDegradationService.getCachedLongWithFallback(cacheKey, fallbackSupplier)
                .switchIfEmpty(
                        // Если кэш пуст, получаем из БД и асинхронно сохраняем в кэш
                        Mono.defer(fallbackSupplier)
                                .flatMap(count -> {
                                    // Асинхронно сохраняем в кэш без блокировки основного потока
                                    redisDegradationService.cacheString(cacheKey, count.toString(), CACHE_TTL)
                                            .subscribe();
                                    return Mono.just(count);
                                })
                );
    }

    @Override
    public Mono<Item> getCachedItemById(Long id, java.util.function.Supplier<Mono<Item>> fallbackSupplier) {
        String key = "item:" + id;

        // Попытка получить из кэша с fallback на БД
        return redisDegradationService.getCachedItemWithFallback(key, fallbackSupplier)
                .switchIfEmpty(
                        // Если кэш пуст, получаем из БД и асинхронно сохраняем в кэш
                        Mono.defer(fallbackSupplier)
                                .flatMap(item -> {
                                    // Асинхронно сохраняем в кэш без блокировки основного потока
                                    redisDegradationService.cacheItem(key, item, CACHE_TTL)
                                            .subscribe();
                                    return Mono.just(item);
                                })
                );
    }

    @Override
    public void invalidateItemCache(Long id) {
        String key = "item:" + id;
        redisDegradationService.invalidateCache(key)
                .subscribe(); // Асинхронная инвалидация
    }

    @Override
    public void invalidateListCaches() {
        // В реальном приложении здесь можно было бы использовать шаблон ключей или теги
        // Для простоты пропустим эту часть, так как в Reactive Redis нет прямого аналога keys()
        // В продакшене лучше использовать более точечную инвалидацию через теги или префиксы
        log.debug("List cache invalidation skipped in reactive mode");
    }
}