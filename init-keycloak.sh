#!/bin/bash

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to start..."
until curl -s http://localhost:8081/health > /dev/null; do
    sleep 5
done

echo "Keycloak is ready. Configuring..."

# Get admin token
ADMIN_TOKEN=$(curl -s -X POST \
  http://localhost:8081/realms/master/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=admin" \
  -d "password=admin" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

# Create realm
curl -s -X POST \
  http://localhost:8081/admin/realms \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "ecommerce-realm",
    "realm": "ecommerce-realm",
    "displayName": "E-commerce Realm",
    "enabled": true,
    "sslRequired": "external",
    "registrationAllowed": true,
    "registrationEmailAsUsername": false,
    "rememberMe": true,
    "verifyEmail": false,
    "loginWithEmailAllowed": true,
    "duplicateEmailsAllowed": false,
    "resetPasswordAllowed": true,
    "editUsernameAllowed": true
  }'

# Create clients for Client Credentials Flow
# E-commerce app client
curl -s -X POST \
  http://localhost:8081/admin/realms/ecommerce-realm/clients \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "ecommerce-app",
    "name": "E-commerce Application",
    "description": "Main e-commerce web application",
    "enabled": true,
    "clientAuthenticatorType": "client-secret",
    "secret": "ecommerce-app-secret",
    "directAccessGrantsEnabled": false,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "publicClient": false,
    "protocol": "openid-connect",
    "redirectUris": [
      "http://localhost:8080/*"
    ],
    "webOrigins": [
      "http://localhost:8080"
    ]
  }'

# Payment service client
curl -s -X POST \
  http://localhost:8081/admin/realms/ecommerce-realm/clients \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "payment-service",
    "name": "Payment Service",
    "description": "Payment processing service",
    "enabled": true,
    "clientAuthenticatorType": "client-secret",
    "secret": "payment-service-secret",
    "directAccessGrantsEnabled": false,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "publicClient": false,
    "protocol": "openid-connect"
  }'

# Create roles
curl -s -X POST \
  http://localhost:8081/admin/realms/ecommerce-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "USER",
    "description": "Regular user"
  }'

curl -s -X POST \
  http://localhost:8081/admin/realms/ecommerce-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "ADMIN",
    "description": "Administrator"
  }'

curl -s -X POST \
  http://localhost:8081/admin/realms/ecommerce-realm/roles \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "PAYMENT_SERVICE",
    "description": "Payment service role"
  }'

echo "Keycloak configuration completed!"
echo ""
echo "Client Credentials for applications:"
echo "------------------------------------"
echo "E-commerce App:"
echo "  Client ID: ecommerce-app"
echo "  Client Secret: ecommerce-app-secret"
echo "  Token Endpoint: http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token"
echo ""
echo "Payment Service:"
echo "  Client ID: payment-service"
echo "  Client Secret: payment-service-secret"
echo "  Token Endpoint: http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/token"
echo ""
echo "JWKS Endpoint: http://localhost:8081/realms/ecommerce-realm/protocol/openid-connect/certs"