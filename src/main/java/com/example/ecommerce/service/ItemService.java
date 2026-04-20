package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Flux<Item> getItemsPage(String search, String sort, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;
        int limit = pageSize;

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
    }

    public Mono<Long> getTotalCount(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return itemRepository.countBySearch(search);
        }
        return itemRepository.count();
    }

    public Mono<Item> getItemById(Long id) {
        log.debug("Fetching item by id: {}", id);
        return itemRepository.findById(id);
    }

    public Flux<Item> getAllItems() {
        log.debug("Fetching all items");
        return itemRepository.findAll();
    }

    public Mono<Item> updateItem(Item item) {
        log.info("Updating item: id={}, title={}", item.getId(), item.getTitle());
        return itemRepository.save(item);
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
                    return itemRepository.save(item);
                });
    }
}