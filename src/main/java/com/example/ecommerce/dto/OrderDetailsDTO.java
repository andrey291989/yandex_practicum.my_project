package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Order;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailsDTO(
        Long id,
        Long totalSum,
        LocalDateTime createdAt,
        List<OrderItemDTO> items
) {
    public static OrderDetailsDTO fromEntity(Order order) {
        return new OrderDetailsDTO(
                order.getId(),
                order.getTotalSum(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(OrderItemDTO::fromEntity)
                        .toList()
        );
    }
}