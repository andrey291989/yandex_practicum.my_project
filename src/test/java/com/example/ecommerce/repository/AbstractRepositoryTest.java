package com.example.ecommerce.repository;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("repository-h2")
public abstract class AbstractRepositoryTest {
    // Базовый класс для репозиторных тестов с H2
    // Testcontainers не используется для репозиторных тестов
}