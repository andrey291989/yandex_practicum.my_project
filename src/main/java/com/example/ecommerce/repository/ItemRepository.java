package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

    @Query("SELECT * FROM items WHERE " +
            "(:search IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Flux<Item> searchItems(@Param("search") String search);

    @Query("SELECT COUNT(*) FROM items WHERE " +
            "(:search IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Mono<Long> countBySearch(@Param("search") String search);

    @Query("SELECT * FROM items WHERE id = :id FOR UPDATE")
    Mono<Item> findByIdWithLock(@Param("id") Long id);
}