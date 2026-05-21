# E-commerce Showcase Application

Демонстрационное приложение интернет-магазина с возможностью просмотра товаров, добавления в корзину и оформления заказов. Приложение состоит из двух основных компонентов: основного веб-приложения и RESTful-сервиса платежей, интегрированных через OAuth2.

## Архитектура проекта

Проект представляет собой мультимодульное приложение с двумя основными компонентами:
- **Основное веб-приложение** (порт 8080) - фронтенд и бизнес-логика интернет-магазина
- **RESTful-сервис платежей** (порт 8082) - backend сервис для обработки платежей

Оба компонента интегрированы через OAuth2 для безопасного взаимодействия.

## Требования

- Java 21
- Maven 3.8+
- Docker и Docker Compose
- 8 ГБ свободной оперативной памяти (для запуска всех сервисов)

## Стек технологий

### Основное приложение
- Spring Boot 3.1.5 (WebFlux - реактивный стек)
- Spring Security (OAuth2 Client и Resource Server)
- PostgreSQL (основная база данных)
- Redis (кэширование с graceful degradation)
- Thymeleaf (серверный рендеринг)
- Bootstrap 5 (фронтенд фреймворк)

### Платежный сервис
- Spring Boot 3.1.5 (WebFlux)
- Spring Security OAuth2 Resource Server
- Встроенный in-memory storage (для демонстрации)

### Инфраструктура
- Keycloak (сервер авторизации OAuth2)
- Docker Compose (оркестрация сервисов)
- Flyway (миграции базы данных)

## Быстрый старт

### Запуск всего стека с помощью Docker

```bash
# Сделайте скрипт исполняемым (если еще не сделано)
chmod +x build-and-run.sh

# Запустите скрипт сборки и запуска
./build-and-run.sh
```

Эта команда выполнит:
1. Сборку Maven проекта
2. Создание Docker образов
3. Запуск всех сервисов через docker-compose

### Ручной запуск

#### 1. Сборка проекта

```bash
# Сборка без запуска тестов (для быстрой сборки)
mvn clean package -DskipTests

# Сборка с запуском всех тестов
mvn clean package
```

#### 2. Сборка Docker образов

```bash
# Сборка образа основного приложения
docker build -t ecommerce-showcase .

# Сборка образа платежного сервиса
docker build -t payment-service -f Dockerfile.payment .
```

#### 3. Запуск с помощью docker-compose

```bash
# Запуск всех сервисов в фоновом режиме
docker-compose up -d

# Просмотр логов
docker-compose logs -f
```

## Доступ к приложению

После запуска сервисы будут доступны по следующим адресам:

### Основное приложение (http://localhost:8080)
- **Главная страница**: http://localhost:8080
- **Каталог товаров**: http://localhost:8080/items
- **Корзина**: http://localhost:8080/cart/items
- **Заказы**: http://localhost:8080/orders
- **API документация**: http://localhost:8080/swagger-ui.html

### Платежный сервис (http://localhost:8082)
- **Health check**: http://localhost:8082/actuator/health
- **Публичный статус**: http://localhost:8082/api/payment/public/health

### Keycloak (http://localhost:8081)
- **Admin Console**: http://localhost:8081/admin/master/console/
  - Username: admin
  - Password: admin
- **User Console**: http://localhost:8081/realms/ecommerce-realm/account/

## Использование приложения

### 1. Первичная настройка

После первого запуска необходимо настроить Keycloak:

```bash
# Сделайте скрипт инициализации исполняемым
chmod +x init-keycloak.sh

# Выполните инициализацию Keycloak
./init-keycloak.sh
```

Этот скрипт создаст:
- Realm `ecommerce-realm`
- Клиент `ecommerce-app` (для основного приложения)
- Клиент `payment-service` (для платежного сервиса)
- Роли пользователей

### 2. Регистрация и вход

1. Перейдите на http://localhost:8080
2. Нажмите "Войти" в правом верхнем углу
3. Выберите "Зарегистрироваться" для создания нового аккаунта
4. После регистрации войдите с вашими учетными данными

### 3. Работа с товарами

1. Просматривайте товары на главной странице
2. Добавляйте товары в корзину с помощью кнопок "+" и "-"
3. Перейдите в корзину для просмотра выбранных товаров
4. Нажмите "Оформить заказ" для создания заказа

### 4. Просмотр заказов

После оформления заказа вы будете перенаправлены на страницу заказа. Все ваши заказы доступны по ссылке "Заказы" в навигационной панели.

## Разработка

### Структура проекта

```
src/
├── main/
│   ├── java/com/example/ecommerce/
│   │   ├── cache/          # Кэширование (Redis)
│   │   ├── config/         # Конфигурации Spring
│   │   ├── controller/     # Контроллеры основного приложения
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # Сущности базы данных
│   │   ├── repository/     # Репозитории данных
│   │   ├── service/        # Сервисы бизнес-логики
│   │   ├── usecase/        # Бизнес-кейсы (оформление заказа)
│   │   └── payment/        # Платежный сервис (отдельный пакет)
│   └── resources/
│       ├── api/            # OpenAPI спецификации
│       ├── db/migration/   # Миграции базы данных (Flyway)
│       └── templates/      # Шаблоны Thymeleaf
└── test/
    └── java/com/example/ecommerce/
        ├── controller/     # Тесты контроллеров
        ├── integration/    # Интеграционные тесты
        ├── repository/     # Тесты репозиториев
        ├── service/        # Тесты сервисов
        └── payment/        # Тесты платежного сервиса
```

### Запуск в режиме разработки

#### Основное приложение

```bash
# Запуск основного приложения
mvn spring-boot:run

# Запуск с определенным профилем
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Платежный сервис (отдельно)

```bash
# Запуск платежного сервиса отдельно
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --spring.profiles.active=payment"
```

### Запуск тестов

#### Все тесты

```bash
# Запуск всех тестов
mvn test

# Запуск всех тестов с подробным выводом
mvn test -X
```

#### По категориям

```bash
# Запуск unit тестов
mvn test -Punit-tests

# Запуск репозиторных тестов
mvn test -Prepository-tests

# Запуск интеграционных тестов
mvn test -Pintegration-tests

# Запуск полных интеграционных тестов (с Testcontainers)
mvn test -Pintegration-full-tests
```

#### Конкретные тесты

```bash
# Запуск конкретного тестового класса
mvn test -Dtest=ItemServiceTest

# Запуск конкретного тестового метода
mvn test -Dtest=ItemServiceTest#testGetItemById

# Запуск тестов по паттерну
mvn test -Dtest=*Payment*
```

### Сборка проекта

#### Быстрая сборка (без тестов)

```bash
mvn clean package -DskipTests
```

#### Полная сборка (с тестами)

```bash
mvn clean package
```

#### Сборка с конкретными профилями

```bash
# Сборка для production
mvn clean package -Pprod

# Сборка для development
mvn clean package -Pdev
```

## Безопасность

### Архитектура безопасности

Проект использует OAuth2 для обеспечения безопасности:

1. **Keycloak** как сервер авторизации
2. **Основное приложение** как OAuth2 клиент (Client Credentials Flow)
3. **Платежный сервис** как OAuth2 Resource Server

### OAuth2 клиенты

#### Основное приложение (ecommerce-app)
- **Client ID**: `ecommerce-app`
- **Client Secret**: `ecommerce-app-secret`
- **Grant Type**: Client Credentials
- **Scope**: `openid`

#### Платежный сервис (payment-service)
- **Client ID**: `payment-service`
- **Client Secret**: `payment-service-secret`
- **Grant Type**: Client Credentials
- **Scope**: `payment-service`

### Аутентификация пользователей

Пользователи могут регистрироваться и входить в систему через форму логина:
- **Страница входа**: http://localhost:8080/login
- **Страница регистрации**: http://localhost:8080/register

Тестовые пользователи:
- **admin** / **password** (роль ADMIN)
- **user** / **password** (роль USER)

### Защищенные ресурсы

#### Основное приложение
- **Защищенные страницы**: `/cart/**`, `/orders/**`, `/buy`
- **Публичные страницы**: `/`, `/items/**`, `/login`, `/register`

#### Платежный сервис
- **Защищенные эндпоинты**: `/api/payments/**` (требуют JWT с scope `payment-service`)
- **Публичные эндпоинты**: `/api/payment/public/**`, `/actuator/**`

### API взаимодействия

#### Получение токена (Client Credentials Flow)

```bash
curl -X POST \
  http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=ecommerce-app" \
  -d "client_secret=ecommerce-app-secret"
```

#### Вызов защищенного API платежного сервиса

```bash
curl -X GET \
  http://localhost:8082/api/payments/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Конфигурация

### Переменные окружения

Основные переменные окружения задаются в `docker-compose.yml`:

```yaml
# База данных
POSTGRES_DB: ecommerce_db
POSTGRES_USER: myuser
POSTGRES_PASSWORD: mypassword

# Redis
REDIS_HOST: redis
REDIS_PORT: 6379
```

### Конфигурационные файлы

- **application.yml** - основная конфигурация
- **application-security.yml** - конфигурация безопасности OAuth2
- **application-payment.yml** - конфигурация платежного сервиса
- **application-local.yml** - локальная конфигурация для разработки

## Миграции базы данных

Flyway автоматически применяет миграции из директории `src/main/resources/db/migration`:

1. `V1__Create_item_table.sql` - таблица товаров
2. `V2__Create_order_tables.sql` - таблицы заказов
3. `V3__Create_payment_table.sql` - таблица платежей
4. `V4__Create_users_table.sql` - таблица пользователей
5. `V5__Insert_default_users.sql` - тестовые пользователи

## Мониторинг и логирование

### Health Checks

- **Основное приложение**: http://localhost:8080/actuator/health
- **Платежный сервис**: http://localhost:8082/actuator/health

### Логирование

Логи доступны через:
- Docker logs: `docker-compose logs -f`
- Файлы логов в контейнерах
- Уровень логирования настраивается в `application.yml`

## Устранение неполадок

### Частые проблемы

#### 1. Keycloak не отвечает
```bash
# Перезапустите сервисы
docker-compose restart keycloak

# Проверьте статус
docker-compose ps
```

#### 2. Ошибка подключения к базе данных
```bash
# Проверьте логи базы данных
docker-compose logs db

# Пересоздайте контейнеры с volumes
docker-compose down -v
docker-compose up -d
```

#### 3. Проблемы с OAuth2
```bash
# Перезапустите Keycloak и выполните инициализацию
docker-compose restart keycloak
./init-keycloak.sh
```

### Полезные команды

```bash
# Просмотр запущенных контейнеров
docker-compose ps

# Просмотр логов всех сервисов
docker-compose logs -f

# Просмотр логов конкретного сервиса
docker-compose logs -f app

# Остановка и удаление всех контейнеров
docker-compose down -v

# Пересборка и перезапуск
docker-compose down
mvn clean package -DskipTests
docker-compose up -d
```

## Разработка и вклад

### Code Style

Проект следует стандартным практикам Spring Boot и Java:
- Использование реактивного стека (Project Reactor)
- Чистая архитектура с разделением на слои
- Dependency Injection через конструкторы
- Использование immutable объектов где возможно

### Тестирование

Проект имеет 4 уровня тестирования:
1. **Unit тесты** - тестирование отдельных компонентов
2. **Repository тесты** - тестирование слоя доступа к данным
3. **Integration тесты** - тестирование взаимодействия компонентов
4. **Full Integration тесты** - end-to-end тесты с Testcontainers

### Contributing

1. Fork репозиторий
2. Создайте feature ветку (`git checkout -b feature/AmazingFeature`)
3. Сделайте commit изменений (`git commit -m 'Add some AmazingFeature'`)
4. Push в ветку (`git push origin feature/AmazingFeature`)
5. Откройте Pull Request

Подробную информацию о настройке и использовании системы безопасности см. в файле [SECURITY-README.md](SECURITY-README.md).