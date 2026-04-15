package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Order;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDTO(
        Long id,
        Long totalSum,
        LocalDateTime createdAt,
        List<OrderItemDTO> items
) {
    public static OrderDTO fromEntity(Order order, List<OrderItemDTO> items) {
        return new OrderDTO(
                order.getId(),
                order.getTotalSum(),
                order.getCreatedAt(),
                items
        );
    }

    public static OrderDTO summaryFromEntity(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getTotalSum(),
                order.getCreatedAt(),
                null
        );
    }
}