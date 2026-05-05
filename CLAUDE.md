# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a reactive e-commerce showcase application built with Spring Boot WebFlux, using reactive programming principles. The application follows RESTful architecture principles and allows users to browse products, add items to a cart, and place orders.

Key technologies:
- Java 21
- Spring Boot 3.1.5 with WebFlux (reactive)
- PostgreSQL with R2DBC for reactive database access
- Flyway for database migrations
- Thymeleaf for server-side rendering
- Docker & Docker Compose for containerization
- Maven for build management

## Architecture

### High-Level Structure
- **Controllers**: Handle HTTP requests and responses (ItemController, CartController, CheckoutController, OrderController)
- **Services**: Business logic layer (ItemService, CartService, CheckoutService, OrderService)
- **Repositories**: Data access layer using Spring Data R2DBC (ItemRepository, OrderRepository, OrderItemRepository)
- **Entities**: Domain objects representing database tables (Item, Order, OrderItem)
- **DTOs**: Data Transfer Objects for presentation layer (ItemDTO, OrderDTO, OrderItemDTO, OrderSummaryDTO, PagingDTO)

### RESTful Design Principles
The application follows RESTful architectural principles:
- **Resources**: Clear resource-oriented URLs (/items, /cart/items, /orders)
- **HTTP Methods**: Proper use of GET, POST, PATCH, DELETE verbs
- **Stateless**: Each request contains all necessary information
- **Cacheable**: Responses can be cached where appropriate
- **Uniform Interface**: Consistent API design across all endpoints
- **Layered System**: Separation of concerns through layered architecture

### Reactive Patterns
The application uses Project Reactor with Mono and Flux for reactive programming:
- Controllers return Mono<String> for single values or Flux<T> for streams
- Service methods compose reactive chains for non-blocking operations
- Database operations use reactive repositories

### Data Model
- **Items**: Products available for purchase with title, description, price, and inventory count
- **Orders**: Customer orders with total sum and creation timestamp
- **OrderItems**: Snapshot of items at time of purchase, linked to orders

### Session Management
Shopping cart is stored in WebSession attributes, making it client-side stateless but session-scoped. This approach maintains RESTful principles by keeping sessions lightweight and allowing horizontal scaling.

## Common Development Tasks

### Building and Running

#### Quick Start (Docker)
```bash
chmod +x build-and-run.sh
./build-and-run.sh
```

#### Manual Build and Run
1. Build the application:
   ```bash
   mvn clean package -DskipTests
   ```

2. Build Docker image:
   ```bash
   docker build -t ecommerce-showcase .
   ```

3. Start with docker-compose:
   ```bash
   docker-compose up -d
   ```

#### Development Mode
```bash
mvn spring-boot:run
```

### Testing
Run all tests:
```bash
mvn test
```

### Database Migrations
Flyway automatically applies migrations from `src/main/resources/db/migration` at startup.

Migration files:
- V1__Create_item_table.sql: Creates items table with sample data
- V2__Create_order_tables.sql: Creates orders and order_items tables

### RESTful API Endpoints

#### Item Resources
- `GET /items` - Retrieve paginated list of items with optional search and sorting
- `GET /items/{id}` - Retrieve specific item details

#### Cart Resources
- `GET /cart/items` - Retrieve current cart contents
- `POST /cart/items/{id}` - Add item to cart
- `POST /cart/items` - Update cart item (add/remove/quantity)
- `PATCH /cart/items/{id}?action=INCREASE|DECREASE` - Modify item quantity
- `DELETE /cart/items/{id}` - Remove item from cart

#### Order Resources
- `GET /orders` - Retrieve all orders
- `GET /orders/{id}` - Retrieve specific order details
- `POST /buy` - Create new order from cart contents

#### HTTP Status Codes
- `200 OK` - Successful GET, PUT, PATCH requests
- `201 Created` - Successful POST requests (resource creation)
- `204 No Content` - Successful DELETE requests
- `400 Bad Request` - Invalid request data
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server-side errors

## Key Implementation Details

### RESTful Resource Representation
- Resources are represented as JSON payloads
- Consistent field naming conventions
- Proper use of HTTP headers for content negotiation
- Hypermedia links between related resources where appropriate

### Inventory Management
- Stock levels are tracked in the `count` field of items
- The `decrementStock()` method in ItemService uses database-level row locking (`FOR UPDATE`) to prevent race conditions
- Checkout process validates stock availability before processing orders

### Transaction Handling
- Order creation uses Spring's reactive transaction management
- The entire checkout process (stock validation, order creation, cart clearing) runs in a single transaction

### Session-Based Cart
- Cart data is stored in WebSession attributes
- Uses ConcurrentHashMap internally for thread safety
- Cart is automatically cleared after successful checkout

### Sorting and Pagination
- Item listing supports search, sorting (by title, price), and pagination
- Custom repository queries implement efficient database-level pagination

## Repository Structure
- `src/main/java/com/example/ecommerce/`: Main source code
- `src/main/resources/`: Configuration, templates, and migrations
- `src/test/java/com/example/ecommerce/`: Unit and integration tests