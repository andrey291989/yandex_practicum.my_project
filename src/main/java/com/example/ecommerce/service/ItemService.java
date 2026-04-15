package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Flux<Item> getItemsPage(String search, String sort, int pageNumber, int pageSize) {
        int offset = (pageNumber - 1) * pageSize;

        Flux<Item> itemsFlux;

        if (search != null && !search.trim().isEmpty()) {
            log.debug("Searching for: {}", search);
            itemsFlux = itemRepository.searchItems(search);
        } else {
            itemsFlux = itemRepository.findAll();
        }

        // Сортировка
        Comparator<Item> comparator;
        if ("ALPHA".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(Item::getTitle, Comparator.nullsLast(String::compareTo));
            log.debug("Sorting by title ascending");
        } else if ("PRICE".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(Item::getPrice, Comparator.nullsLast(Long::compareTo));
            log.debug("Sorting by price ascending");
        } else {
            comparator = Comparator.comparing(Item::getId);
            log.debug("No sorting applied");
        }

        return itemsFlux
                .sort(comparator)
                .skip(offset)
                .take(pageSize);
    }

    public Mono<Long> getTotalCount(String search) {
        if (search != null && !search.trim().isEmpty()) {
            return itemRepository.countBySearch(search);
        } else {
            return itemRepository.count();
        }
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
}