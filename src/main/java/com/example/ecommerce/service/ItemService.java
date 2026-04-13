package com.example.ecommerce.service;

import com.example.ecommerce.entity.Item;
import com.example.ecommerce.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ItemService {

    private static final Logger log = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Page<Item> getItemsPage(String search, String sort, int pageNumber, int pageSize) {
        int zeroBasedPage = pageNumber - 1;

        Sort sortObj = Sort.unsorted();
        if ("ALPHA".equalsIgnoreCase(sort)) {
            sortObj = Sort.by("title").ascending();
            log.debug("Sorting by title ascending");
        } else if ("PRICE".equalsIgnoreCase(sort)) {
            sortObj = Sort.by("price").ascending();
            log.debug("Sorting by price ascending");
        } else {
            log.debug("No sorting applied");
        }

        PageRequest pageRequest = PageRequest.of(zeroBasedPage, pageSize, sortObj);

        Page<Item> result;
        if (search != null && !search.trim().isEmpty()) {
            log.debug("Searching for: {}", search);
            result = itemRepository.searchItems(search, pageRequest);
        } else {
            result = itemRepository.findAll(pageRequest);
        }

        log.info("Returned {} items (page {} of {}, total {})",
                result.getNumberOfElements(), pageNumber, result.getTotalPages(), result.getTotalElements());

        return result;
    }

    public Item getItemById(Long id) {
        log.debug("Fetching item by id: {}", id);
        return itemRepository.findById(id).orElse(null);
    }

    public List<Item> getAllItems() {
        log.debug("Fetching all items");
        return itemRepository.findAll();
    }

    @Transactional
    public void updateItem(Item item) {
        log.info("Updating item: id={}, title={}", item.getId(), item.getTitle());
        itemRepository.save(item);
    }
}