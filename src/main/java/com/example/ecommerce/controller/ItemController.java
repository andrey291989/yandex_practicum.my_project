package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ItemDTO;
import com.example.ecommerce.dto.PagingDTO;
import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public Mono<String> redirectToItems() {
        return Mono.just("redirect:/items");
    }

    @GetMapping("/items")
    public Mono<String> getItems(
            WebSession session,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            Model model) {

        int validPageNumber = Math.max(pageNumber, 1);
        int validPageSize = Math.max(pageSize, 1);

        log.info("GET /items - search: {}, sort: {}, page: {}, size: {}",
                search, sort, validPageNumber, validPageSize);

        return Mono.zip(
                        itemService.getItemsPage(search, sort, validPageNumber, validPageSize)
                                .collectList(),
                        itemService.getTotalCount(search),
                        cartService.getCartItems(session)
                )
                .flatMap(tuple -> {
                    List<ItemDTO> items = tuple.getT1().stream()
                            .map(item -> {
                                int cartCount = tuple.getT3().getOrDefault(item.getId(), 0);
                                return ItemDTO.fromEntity(item, cartCount);
                            })
                            .collect(Collectors.toList());

                    long totalElements = tuple.getT2();
                    int totalPages = (int) Math.ceil((double) totalElements / validPageSize);
                    boolean hasPrevious = validPageNumber > 1;
                    boolean hasNext = validPageNumber < totalPages;

                    PagingDTO paging = new PagingDTO(
                            validPageSize,
                            validPageNumber,
                            hasPrevious,
                            hasNext,
                            totalElements,
                            totalPages
                    );

                    model.addAttribute("items", items);
                    model.addAttribute("search", search != null ? search : "");
                    model.addAttribute("sort", sort);
                    model.addAttribute("paging", paging);

                    return Mono.just("items");
                });
    }

    @GetMapping("/items/{id}")
    public Mono<String> getItemDetails(
            WebSession session,
            @PathVariable Long id,
            Model model) {

        log.info("GET /items/{}", id);

        return itemService.getItemById(id)
                .flatMap(item -> cartService.getCartItems(session)
                        .map(cartItems -> {
                            int cartCount = cartItems.getOrDefault(id, 0);
                            ItemDTO itemDTO = ItemDTO.fromEntity(item, cartCount);
                            model.addAttribute("item", itemDTO);
                            return "item";
                        }))
                .switchIfEmpty(Mono.just("redirect:/items"));
    }
}