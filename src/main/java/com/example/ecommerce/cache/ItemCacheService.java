package com.example.ecommerce.cache;

import com.example.ecommerce.entity.Item;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Интерфейс сервиса кэширования для товаров
 * Предоставляет абстракцию над механизмом кэширования
 */
public interface ItemCacheService {

    /**
     * Получить страницу товаров из кэша
     * @param search поисковый запрос
     * @param sort тип сортировки
     * @param pageNumber номер страницы
     * @param pageSize размер страницы
     * @param fallbackSupplier поставщик данных при отсутствии в кэше
     * @return_flux с товарами
     */
    Flux<Item> getCachedItemsPage(String search, String sort, int pageNumber, int pageSize,
                                  java.util.function.Supplier<Flux<Item>> fallbackSupplier);

    /**
     * Получить общее количество товаров из кэша
     * @param search поисковый запрос
     * @param fallbackSupplier поставщик данных при отсутствии в кэше
     * @return_mono с количеством товаров
     */
    Mono<Long> getCachedTotalCount(String search, java.util.function.Supplier<Mono<Long>> fallbackSupplier);

    /**
     * Получить товар по ID из кэша
     * @param id ID товара
     * @param fallbackSupplier поставщик данных при отсутствии в кэше
     * @return_mono с товаром
     */
    Mono<Item> getCachedItemById(Long id, java.util.function.Supplier<Mono<Item>> fallbackSupplier);

    /**
     * Инвалидировать кэш для конкретного товара
     * @param id ID товара
     */
    void invalidateItemCache(Long id);

    /**
     * Инвалидировать кэши списков товаров
     */
    void invalidateListCaches();
}