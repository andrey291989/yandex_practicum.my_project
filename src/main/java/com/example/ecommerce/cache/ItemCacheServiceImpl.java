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
    private static final Duration LIST_CACHE_TTL = Duration.ofMinutes(2);

    private final RedisGracefulDegradationService redisDegradationService;

    public ItemCacheServiceImpl(RedisGracefulDegradationService redisDegradationService) {
        this.redisDegradationService = redisDegradationService;
    }

    @Override
    public Flux<Item> getCachedItemsPage(String search, String sort, int pageNumber, int pageSize,
                                        java.util.function.Supplier<Flux<Item>> fallbackSupplier) {
        String cacheKey = String.format("items:page:%s:%s:%d:%d",
                search != null ? search : "all",
                sort != null ? sort : "ALPHA",
                pageNumber, pageSize);

        return redisDegradationService.getCachedListWithFallback(cacheKey, () ->
                Flux.defer(fallbackSupplier)
                        .collectList()
                        .flatMapMany(items -> {
                            if (!items.isEmpty()) {
                                redisDegradationService.cacheList(
                                        cacheKey, Flux.fromIterable(items), LIST_CACHE_TTL)
                                        .subscribe(null,
                                                e -> log.warn("Async cache write failed for key={}", cacheKey, e));
                            }
                            return Flux.fromIterable(items);
                        })
        );
    }

    @Override
    public Mono<Long> getCachedTotalCount(String search, java.util.function.Supplier<Mono<Long>> fallbackSupplier) {
        String cacheKey = "items:count:" + (search != null ? search : "all");

        return redisDegradationService.getCachedLongWithFallback(cacheKey, () ->
                Mono.defer(fallbackSupplier)
                        .flatMap(count -> {
                            redisDegradationService.cacheString(cacheKey, count.toString(), LIST_CACHE_TTL)
                                    .subscribe(null,
                                            e -> log.warn("Async cache write failed for key={}", cacheKey, e));
                            return Mono.just(count);
                        })
        );
    }

    @Override
    public Mono<Item> getCachedItemById(Long id, java.util.function.Supplier<Mono<Item>> fallbackSupplier) {
        String key = "item:" + id;

        return redisDegradationService.getCachedItemWithFallback(key, () ->
                Mono.defer(fallbackSupplier)
                        .flatMap(item -> {
                            redisDegradationService.cacheItem(key, item, CACHE_TTL)
                                    .subscribe(null,
                                            e -> log.warn("Async cache write failed for key={}", key, e));
                            return Mono.just(item);
                        })
        );
    }

    @Override
    public void invalidateItemCache(Long id) {
        String key = "item:" + id;
        redisDegradationService.invalidateCache(key)
                .subscribe(null, e -> log.warn("Async cache invalidation failed for key={}", key, e));
    }

    @Override
    public void invalidateListCaches() {
        log.debug("List cache invalidation skipped — relies on short TTL ({})", LIST_CACHE_TTL);
    }
}