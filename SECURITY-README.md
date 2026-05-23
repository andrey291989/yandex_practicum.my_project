# Настройка OAuth2 и Keycloak

## Запуск системы

1. Запустите все сервисы с помощью Docker Compose:
   ```bash
   docker-compose up -d
   ```

2. Дождитесь запуска Keycloak (примерно 30-60 секунд)

3. Выполните скрипт инициализации Keycloak:
   ```bash
   ./init-keycloak.sh
   ```

## Параметры подключения к Keycloak

После выполнения скрипта инициализации будут созданы следующие клиенты:

### E-commerce Application
- **Client ID**: `ecommerce-app`
- **Client Secret**: `ecommerce-app-secret`
- **Token Endpoint**: `http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token`
- **JWKS Endpoint**: `http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/certs`

### Payment Service
- **Client ID**: `payment-service`
- **Client Secret**: `payment-service-secret`
- **Token Endpoint**: `http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token`
- **JWKS Endpoint**: `http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/certs`

## Использование Client Credentials Flow

### Получение токена для e-commerce приложения:
```bash
curl -X POST \
  http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=ecommerce-app" \
  -d "client_secret=ecommerce-app-secret"
```

### Получение токена для сервиса платежей:
```bash
curl -X POST \
  http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials" \
  -d "client_id=payment-service" \
  -d "client_secret=payment-service-secret"
```

## Доступ к защищенным ресурсам

После получения токена, вы можете получить доступ к защищенным ресурсам, передав токен в заголовке Authorization:

```bash
curl -X GET \
  http://localhost:8080/api/secure/payment-status \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"
```

## Адреса сервисов

- **Основное приложение**: http://localhost:8080
- **Сервис платежей**: http://localhost:8082
- **Keycloak Admin Console**: http://localhost:8081/admin/master/console/
  - Username: admin
  - Password: admin
- **Keycloak User Console**: http://localhost:8081/realms/ecommerce-realm/account/

## Тестирование

Вы можете протестировать работу системы после настройки:

1. Получите токен для e-commerce приложения
2. Используйте токен для вызова защищенного эндпоинта:
   ```bash
   curl -X GET http://localhost:8080/api/secure/payment-status \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
   ```