package com.example.ecommerce.dto;

import com.example.ecommerce.entity.OrderItem;

public record OrderItemDTO(
        Long id,
        String title,
        String description,
        String imgPath,
        Long price,
        Integer count
) {
    public static OrderItemDTO fromEntity(OrderItem orderItem) {
        return new OrderItemDTO(
                orderItem.getId(),
                orderItem.getTitle(),
                orderItem.getDescription(),
                orderItem.getImgPath(),
                orderItem.getPrice(),
                orderItem.getCount()
        );
    }
}