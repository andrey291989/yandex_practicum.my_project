#!/bin/bash

# Build and run script for E-commerce Showcase Application

echo "Building Maven project..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "Maven project built successfully!"
else
    echo "Failed to build Maven project!"
    exit 1
fi

echo "Building Docker images..."
docker build -t ecommerce-showcase -f Dockerfile.app .
if [ $? -ne 0 ]; then
    echo "Failed to build ecommerce-showcase Docker image!"
    exit 1
fi

docker build -t payment-service -f Dockerfile.payment .
if [ $? -ne 0 ]; then
    echo "Failed to build payment-service Docker image!"
    exit 1
fi

echo "Docker images built successfully!"

echo "Starting application with Docker Compose..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "Application started successfully!"
    echo "Access the main application at: http://localhost:8080"
    echo "Access the payment service at: http://localhost:8082"
else
    echo "Failed to start application!"
    exit 1
fi