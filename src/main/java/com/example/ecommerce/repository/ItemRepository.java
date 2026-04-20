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
            "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY title ASC " +
            "LIMIT :limit OFFSET :offset")
    Flux<Item> searchItemsWithPagination(@Param("search") String search,
                                         @Param("limit") int limit,
                                         @Param("offset") int offset);

    @Query("SELECT COUNT(*) FROM items WHERE " +
            "(:search IS NULL OR LOWER(title) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "OR LOWER(description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Mono<Long> countBySearch(@Param("search") String search);

    @Query("SELECT * FROM items WHERE id = :id FOR UPDATE")
    Mono<Item> findByIdWithLock(@Param("id") Long id);

    @Query("SELECT * FROM items ORDER BY title ASC LIMIT :limit OFFSET :offset")
    Flux<Item> findAllSortedByTitleAsc(@Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT * FROM items ORDER BY title DESC LIMIT :limit OFFSET :offset")
    Flux<Item> findAllSortedByTitleDesc(@Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT * FROM items ORDER BY price ASC LIMIT :limit OFFSET :offset")
    Flux<Item> findAllSortedByPriceAsc(@Param("limit") int limit, @Param("offset") int offset);

    @Query("SELECT * FROM items ORDER BY price DESC LIMIT :limit OFFSET :offset")
    Flux<Item> findAllSortedByPriceDesc(@Param("limit") int limit, @Param("offset") int offset);

    // Default pagination (backward compatibility)
    default Flux<Item> findAllWithPagination(int limit, int offset) {
        return findAllSortedByTitleAsc(limit, offset);
    }
}