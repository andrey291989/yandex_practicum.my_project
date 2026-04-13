package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ItemDTO;
import com.example.ecommerce.dto.PagingDTO;
import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final CartService cartService;

    public ItemController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
        this.cartService = cartService;
    }

    @GetMapping("/")
    public String redirectToItems() {
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String getItems(
            HttpSession session,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            Model model) {

        if (pageNumber < 1) pageNumber = 1;
        if (pageSize < 1) pageSize = 10;

        Page<Item> itemPage = itemService.getItemsPage(search, sort, pageNumber, pageSize);
        Map<Long, Integer> cartItems = cartService.getCartItems(session);

        // Преобразуем Item в ItemDTO и добавляем количество в корзине
        List<ItemDTO> items = new ArrayList<>();
        for (Item item : itemPage.getContent()) {
            int cartCount = cartItems.getOrDefault(item.getId(), 0);
            items.add(new ItemDTO(
                    item.getId(),
                    item.getTitle(),
                    item.getDescription(),
                    item.getImgPath(),
                    item.getPrice(),
                    item.getCount(),  // stockCount
                    cartCount
            ));
        }

        PagingDTO paging = new PagingDTO(
                pageSize,
                pageNumber,
                pageNumber > 1,
                itemPage.hasNext()
        );

        model.addAttribute("items", items);  // Теперь передаем плоский список
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("sort", sort);
        model.addAttribute("paging", paging);

        return "items";
    }

    @GetMapping("/items/{id}")
    public String getItemDetails(HttpSession session, @PathVariable Long id, Model model) {
        Item item = itemService.getItemById(id);
        if (item == null) {
            return "redirect:/items";
        }

        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        int cartCount = cartItems.getOrDefault(id, 0);

        model.addAttribute("item", new ItemDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                item.getCount(),
                cartCount
        ));
        return "item";
    }
}