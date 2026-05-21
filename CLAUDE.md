# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## High-Level Architecture

This is a reactive e-commerce Spring Boot application built with Java 21 and Maven. The architecture follows a layered approach with clear separation of concerns:

- **Controllers**: Handle HTTP requests (REST endpoints)
- **Services**: Implement business logic
- **Repositories**: Handle data persistence using Spring Data R2DBC
- **Entities**: Domain models mapped to database tables
- **DTOs**: Data Transfer Objects for API responses
- **Use Cases**: Complex business workflows (particularly checkout process)
- **Caching**: Redis-based caching with graceful degradation

Key architectural patterns:
1. Reactive programming model using WebFlux and Project Reactor
2. Use Case pattern for complex business workflows
3. Caching with graceful degradation when Redis is unavailable
4. Layered architecture with dependency injection

## Technology Stack

- **Backend**: Spring Boot 3.1.5 with WebFlux (reactive)
- **Language**: Java 21
- **Build Tool**: Maven
- **Database**: PostgreSQL with R2DBC for reactive connectivity
- **Caching**: Redis with graceful degradation
- **API Documentation**: OpenAPI/Swagger with SpringDoc
- **Testing**: JUnit 5 with Testcontainers for integration tests

## Common Development Commands

### Building and Running

```bash
# Quick build and run with Docker
chmod +x build-and-run.sh
./build-and-run.sh

# Manual build
mvn clean package -DskipTests

# Build Docker image
docker build -t ecommerce-showcase .

# Run with Docker Compose
docker-compose up -d

# Run in development mode
mvn spring-boot:run
```

### Testing

```bash
# Run all tests
mvn test

# Run specific test profiles
mvn test -Punit-tests          # Unit tests only
mvn test -Prepository-tests    # Repository tests only
mvn test -Pintegration-tests   # Integration tests only
mvn test -Pintegration-full-tests  # Full integration tests with Testcontainers

# Run a single test class
mvn test -Dtest=ItemServiceTest
```

### Database Migrations

Flyway automatically applies migrations from `src/main/resources/db/migration` at startup.

### Accessing the Application

- Main storefront: http://localhost:8080
- Orders list: http://localhost:8080/orders
- Cart: http://localhost:8080/cart/items
- API Documentation: http://localhost:8080/swagger-ui.html

## Key Components

### Core Services
- `ItemService`: Product catalog management with caching
- `CartService`: Shopping cart functionality
- `OrderService`: Order management
- `CheckoutService`: Checkout orchestration

### Caching Strategy
- Uses Redis for performance optimization
- Implements graceful degradation - continues operating even when Redis is unavailable
- Different TTL values for individual items (30 min) vs lists (2 min)
- Automatic cache invalidation on data changes

### Checkout Workflow
- Implemented as a use case with distinct steps:
  1. Cart validation
  2. Stock validation
  3. Order calculation
  4. Order creation
  5. Order items creation
- Executes within a database transaction

## Development Environment

Required tools:
- Java 21
- Maven 3.8+
- Docker and Docker Compose
- PostgreSQL (when running without Docker)
- Redis (when running without Docker)

Configuration is managed through:
- `.env` file for database credentials
- Environment variables in `docker-compose.yml`
- Spring Boot configuration properties

## Testing Strategy

Four distinct test profiles:
1. Unit tests (`@Tag("unit")`) - Isolated component testing
2. Repository tests (`@Tag("repository")`) - Data access layer validation
3. Integration tests (`@Tag("integration")`) - Service-level integration
4. Full integration tests (`@Tag("integration-full")`) - End-to-end with Testcontainers

Use appropriate Maven profiles to run specific test types.