package com.example.ecommerce.repository;

import com.example.ecommerce.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<User, Long> {

    @Query("SELECT * FROM users WHERE username = :username")
    Mono<User> findByUsername(String username);

    Mono<User> findByUsernameAndEnabledTrue(String username);
}