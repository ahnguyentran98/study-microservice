#!/bin/bash

echo "Building all microservices..."

# Build Java services
services=("user-service" "product-service" "order-service" "payment-service" "notification-service" "api-gateway")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd "$service"
    ./gradlew clean build -x test
    if [ $? -eq 0 ]; then
        echo "✅ $service built successfully"
    else
        echo "❌ $service build failed"
        exit 1
    fi
    cd ..
done

echo "All services built successfully!"
echo "Run 'docker-compose up --build' to start all services"