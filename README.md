# E-commerce Showcase Application

Демонстрационное приложение интернет-магазина с возможностью просмотра товаров, добавления в корзину и оформления заказов.

## Требования

- Java 21
- Maven 3.8+
- Docker и Docker Compose
- PostgreSQL (при запуске без Docker)

## Сборка и запуск с помощью Docker

### Быстрый запуск

```bash
# Сделайте скрипт исполняемым (если еще не сделано)
chmod +x build-and-run.sh

# Запустите скрипт сборки и запуска
./build-and-run.sh
```

### Ручной запуск

1. Сборка приложения:
```bash
mvn clean package -DskipTests
```

2. Сборка Docker образа:
```bash
docker build -t ecommerce-showcase .
```

3. Запуск с помощью docker-compose:
```bash
docker-compose up -d
```

После запуска приложение будет доступно по адресу: http://localhost:8080

## Остановка приложения

```bash
docker-compose down
```

Для остановки с удалением данных базы данных:
```bash
docker-compose down -v
```

## Конфигурация базы данных

Приложение использует PostgreSQL. Параметры подключения можно изменить в файле `docker-compose.yml`:

- Хост: db (внутри Docker network)
- Порт: 5432
- База данных: ecommerce_db
- Пользователь: myuser
- Пароль: mypassword

## Миграции базы данных

Flyway автоматически применяет миграции из директории `src/main/resources/db/migration` при запуске приложения.

## Доступ к приложению

После запуска приложение доступно по адресу:
- Главная страница (витрина товаров): http://localhost:8080
- Список заказов: http://localhost:8080/orders
- Корзина: http://localhost:8080/cart/items

## Разработка

### Запуск в режиме разработки

```bash
mvn spring-boot:run
```

### Запуск тестов

```bash
mvn test
```