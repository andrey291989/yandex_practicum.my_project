package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Item;

public record ItemDTO(
        Long id,
        String title,
        String description,
        String imgPath,
        Long price,
        int stockCount,
        int cartCount
) {
    public static ItemDTO fromEntity(Item item, int cartCount) {
        if (item == null) {
            return null;
        }
        return new ItemDTO(
                item.getId(),
                item.getTitle() != null ? item.getTitle() : "",
                item.getDescription() != null ? item.getDescription() : "",
                item.getImgPath() != null ? item.getImgPath() : "",
                item.getPrice() != null ? item.getPrice() : 0L,
                item.getCount() != null ? item.getCount() : 0,
                cartCount
        );
    }

    public static ItemDTO fromEntity(Item item) {
        return fromEntity(item, 0);
    }
}