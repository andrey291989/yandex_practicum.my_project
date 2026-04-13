package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ItemDTO;
import com.example.ecommerce.entity.Item;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ItemService itemService;

    public CartController(CartService cartService, ItemService itemService) {
        this.cartService = cartService;
        this.itemService = itemService;
    }

    @GetMapping("/items")
    public String getCartItems(HttpSession session, Model model) {
        Map<Long, Integer> cartItems = cartService.getCartItems(session);
        List<ItemDTO> items = new ArrayList<>();
        long total = 0;

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Item item = itemService.getItemById(entry.getKey());
            if (item != null) {
                items.add(new ItemDTO(
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getImgPath(),
                        item.getPrice(),
                        item.getCount(),
                        entry.getValue()
                ));
                total += item.getPrice() * entry.getValue();
            }
        }

        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/items")
    public String updateCartItem(
            HttpSession session,
            @RequestParam Long id,
            @RequestParam String action,
            @RequestParam(required = false) String from,  // Новый параметр: откуда пришел запрос (cart или items)
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "ALPHA") String sort,
            @RequestParam(required = false, defaultValue = "1") int pageNumber,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            RedirectAttributes redirectAttributes) {

        try {
            if ("PLUS".equals(action)) {
                if (!cartService.checkStockAvailability(id, 1)) {
                    int available = cartService.getAvailableStock(id);
                    redirectAttributes.addFlashAttribute("error",
                            "Недостаточно товара на складе. Доступно: " + available + " шт.");
                    return "redirect:/cart/items";
                }
                cartService.addToCart(session, id);
            } else if ("MINUS".equals(action)) {
                cartService.decreaseQuantity(session, id);
            } else if ("DELETE".equals(action)) {
                cartService.removeFromCart(session, id);
                return "redirect:/cart/items";
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart/items";
        }

        // Определяем, куда редиректить
        if ("cart".equals(from)) {
            // Если пришли из корзины - возвращаемся в корзину
            return "redirect:/cart/items";
        } else {
            // Если пришли с витрины - возвращаемся на витрину с сохранением параметров
            StringBuilder redirect = new StringBuilder("redirect:/items");
            boolean hasParams = false;

            if (search != null && !search.isEmpty()) {
                redirect.append("?search=").append(search);
                hasParams = true;
            }
            if (sort != null && !"ALPHA".equals(sort)) {
                redirect.append(hasParams ? "&" : "?").append("sort=").append(sort);
                hasParams = true;
            }
            if (pageSize != 10) {
                redirect.append(hasParams ? "&" : "?").append("pageSize=").append(pageSize);
                hasParams = true;
            }
            if (pageNumber != 1) {
                redirect.append(hasParams ? "&" : "?").append("pageNumber=").append(pageNumber);
                hasParams = true;
            }

            return redirect.toString();
        }
    }
}