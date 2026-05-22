package com.example.ecommerce.dto;

import com.example.ecommerce.entity.Order;
import java.time.LocalDateTime;

public record OrderSummaryDTO(
        Long id,
        Long totalSum,
        LocalDateTime createdAt
) {
    public static OrderSummaryDTO fromEntity(Order order) {
        return new OrderSummaryDTO(
                order.getId(),
                order.getTotalSum(),
                order.getCreatedAt()
        );
    }
}