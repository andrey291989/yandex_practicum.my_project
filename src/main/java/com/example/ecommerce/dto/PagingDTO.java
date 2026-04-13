package com.example.ecommerce.dto;

public record PagingDTO(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext
) {}