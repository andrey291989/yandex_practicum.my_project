package com.example.ecommerce.dto;

public record PagingDTO(
        int pageSize,
        int pageNumber,
        boolean hasPrevious,
        boolean hasNext,
        long totalElements,
        int totalPages
) {
    public PagingDTO(int pageSize, int pageNumber, boolean hasPrevious, boolean hasNext) {
        this(pageSize, pageNumber, hasPrevious, hasNext, 0, 0);
    }

    public PagingDTO(int pageSize, int pageNumber, boolean hasPrevious, boolean hasNext, long totalElements, int totalPages) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }
}