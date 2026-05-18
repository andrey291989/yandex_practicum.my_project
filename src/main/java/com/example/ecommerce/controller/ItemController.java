package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ItemDTO;
import com.example.ecommerce.dto.PagingDTO;
import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Item", description = "Operations related to items/products")
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @Operation(summary = "Redirect to items page", description = "Redirects to the main items catalog page")
    @ApiResponse(responseCode = "302", description = "Redirect to items page")
    @GetMapping("/")
    public Mono<String> redirectToItems() {
        return Mono.just("redirect:/items");
    }

    @Operation(
        summary = "Get items catalog",
        description = "Retrieves a paginated list of items with optional search and sorting"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved items")
    @GetMapping("/items")
    public Mono<String> getItems(
            WebSession session,
            @Parameter(description = "Search term to filter items") @RequestParam(required = false) String search,
            @Parameter(description = "Sort order (ALPHA for alphabetical, PRICE_ASC for price ascending, PRICE_DESC for price descending)") @RequestParam(required = false, defaultValue = "ALPHA") String sort,
            @Parameter(description = "Page number (starting from 1)") @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @Parameter(description = "Number of items per page") @RequestParam(required = false, defaultValue = "10") int pageSize,
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

    @Operation(
        summary = "Get item details",
        description = "Retrieves detailed information about a specific item by ID"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved item details")
    @ApiResponse(responseCode = "302", description = "Redirect to items page if item not found")
    @GetMapping("/items/{id}")
    public Mono<String> getItemDetails(
            WebSession session,
            @Parameter(description = "Item ID") @PathVariable Long id,
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