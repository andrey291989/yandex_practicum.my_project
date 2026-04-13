package com.example.ecommerce.dto;

public record ItemDTO(
        Long id,
        String title,
        String description,
        String imgPath,
        Long price,
        int stockCount,
        int cartCount
) {
    public static ItemDTO fromEntity(com.example.ecommerce.entity.Item item, int cartCount) {
        return new ItemDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                item.getCount(),
                cartCount
        );
    }
}