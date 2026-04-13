#!/bin/bash

# Build and run script for E-commerce Showcase Application

echo "Building the Docker image..."
docker build -t ecommerce-showcase .

if [ $? -eq 0 ]; then
    echo "Docker image built successfully!"
else
    echo "Failed to build Docker image!"
    exit 1
fi

echo "Starting application with Docker Compose..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo "Application started successfully!"
    echo "Access the application at: http://localhost:8080"
else
    echo "Failed to start application!"
    exit 1
fi