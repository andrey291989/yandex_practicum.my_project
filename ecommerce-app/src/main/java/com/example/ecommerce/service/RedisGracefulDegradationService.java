package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Сервис для работы с Redis с graceful degradation.
 * При недоступности Redis операции продолжаются через основное хранилище.
 */
@Service
public class RedisGracefulDegradationService {

    private static final Logger log = LoggerFactory.getLogger(RedisGracefulDegradationService.class);

    private final ReactiveRedisTemplate<String, Item> itemRedisTemplate;
    private final ReactiveRedisTemplate<String, String> stringRedisTemplate;

    public RedisGracefulDegradationService(ReactiveRedisTemplate<String, Item> itemRedisTemplate,
                                         ReactiveRedisTemplate<String, String> stringRedisTemplate) {
        this.itemRedisTemplate = itemRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * Получение списка элементов из кэша с fallback на основное хранилище
     */
    public Flux<Item> getCachedListWithFallback(String cacheKey,
                                               java.util.function.Supplier<Flux<Item>> fallbackSupplier) {
        return itemRedisTemplate.opsForList().range(cacheKey, 0, -1)
            .doOnNext(item -> log.debug("Items found in cache for key: {}", cacheKey))
            .onErrorResume(throwable -> {
                log.warn("Failed to get items from cache, falling back to database: {}", throwable.getMessage());
                return Flux.empty();
            })
            .switchIfEmpty(
                Flux.defer(fallbackSupplier)
                    .doOnSubscribe(subscription -> log.debug("Cache miss for key: {}, falling back to database", cacheKey))
            );
    }

    /**
     * Сохранение списка элементов в кэш с игнорированием ошибок
     */
    public Mono<Void> cacheList(String cacheKey, Flux<Item> items, Duration ttl) {
        return items.collectList()
            .flatMap(itemList -> {
                if (itemList.isEmpty()) {
                    return Mono.empty();
                }
                return itemRedisTemplate.opsForList().leftPushAll(cacheKey, itemList)
                    .then(itemRedisTemplate.expire(cacheKey, ttl))
                    .doOnSuccess(aVoid -> log.debug("Items cached successfully for key: {}", cacheKey))
                    .onErrorResume(throwable -> {
                        log.warn("Failed to cache items for key: {}, ignoring error: {}", cacheKey, throwable.getMessage());
                        return Mono.empty();
                    })
                    .thenMany(Flux.fromIterable(itemList))
                    .then();
            })
            .onErrorResume(throwable -> {
                log.warn("Failed to cache items for key: {}, ignoring error: {}", cacheKey, throwable.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Получение значения из строкового кэша с fallback
     */
    public Mono<Long> getCachedLongWithFallback(String cacheKey,
                                               java.util.function.Supplier<Mono<Long>> fallbackSupplier) {
        return stringRedisTemplate.opsForValue().get(cacheKey)
            .flatMap(value -> {
                try {
                    Long count = Long.parseLong(value);
                    return Mono.just(count);
                } catch (NumberFormatException e) {
                    return Mono.empty();
                }
            })
            .doOnNext(count -> log.debug("Cached value found for key: {}", cacheKey))
            .onErrorResume(throwable -> {
                log.warn("Failed to get cached value for key: {}, falling back to database: {}", cacheKey, throwable.getMessage());
                return Mono.empty();
            })
            .switchIfEmpty(
                Mono.defer(fallbackSupplier)
                    .doOnSubscribe(subscription -> log.debug("Cache miss for key: {}, falling back to database", cacheKey))
            );
    }

    /**
     * Сохранение строкового значения в кэш с игнорированием ошибок
     */
    public Mono<Void> cacheString(String cacheKey, String value, Duration ttl) {
        return stringRedisTemplate.opsForValue().set(cacheKey, value)
            .then(stringRedisTemplate.expire(cacheKey, ttl))
            .doOnSuccess(aVoid -> log.debug("Value cached successfully for key: {}", cacheKey))
            .then()
            .onErrorResume(throwable -> {
                log.warn("Failed to cache value for key: {}, ignoring error: {}", cacheKey, throwable.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Получение элемента из кэша с fallback
     */
    public Mono<Item> getCachedItemWithFallback(String cacheKey,
                                               java.util.function.Supplier<Mono<Item>> fallbackSupplier) {
        return itemRedisTemplate.opsForValue().get(cacheKey)
            .doOnNext(item -> log.debug("Item found in cache for key: {}", cacheKey))
            .onErrorResume(throwable -> {
                log.warn("Failed to get item from cache, falling back to database: {}", throwable.getMessage());
                return Mono.empty();
            })
            .switchIfEmpty(
                Mono.defer(fallbackSupplier)
                    .doOnSubscribe(subscription -> log.debug("Cache miss for key: {}, falling back to database", cacheKey))
            );
    }

    /**
     * Сохранение элемента в кэш с игнорированием ошибок
     */
    public Mono<Void> cacheItem(String cacheKey, Item item, Duration ttl) {
        return itemRedisTemplate.opsForValue().set(cacheKey, item, ttl)
            .doOnSuccess(aVoid -> log.debug("Item cached successfully for key: {}", cacheKey))
            .then()
            .onErrorResume(throwable -> {
                log.warn("Failed to cache item for key: {}, ignoring error: {}", cacheKey, throwable.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Удаление ключа из кэша с игнорированием ошибок
     */
    public Mono<Void> invalidateCache(String key) {
        return itemRedisTemplate.delete(key)
            .then()
            .doOnSuccess(aVoid -> log.debug("Cache invalidated for key: {}", key))
            .onErrorResume(throwable -> {
                log.warn("Failed to invalidate cache for key: {}, ignoring error: {}", key, throwable.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Удаление строкового ключа из кэша с игнорированием ошибок
     */
    public Mono<Void> invalidateStringCache(String key) {
        return stringRedisTemplate.delete(key)
            .then()
            .doOnSuccess(aVoid -> log.debug("String cache invalidated for key: {}", key))
            .onErrorResume(throwable -> {
                log.warn("Failed to invalidate string cache for key: {}, ignoring error: {}", key, throwable.getMessage());
                return Mono.empty();
            });
    }
}