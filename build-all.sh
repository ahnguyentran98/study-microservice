#!/bin/bash

echo "Building all microservices..."

# Stop any existing Gradle daemons
gradle --stop

# Build Java services
services=("user-service" "product-service" "order-service" "payment-service" "notification-service" "api-gateway")

for service in "${services[@]}"; do
    echo "Building $service..."
    cd "$service"
    
    # Check if wrapper exists and is correct version
    if [ ! -f "gradlew" ] || ! ./gradlew --version | grep -q "Gradle 7"; then
        echo "Updating Gradle wrapper for $service..."
        gradle wrapper --gradle-version 7.6.1
        chmod +x gradlew
    fi
    
    # Build with the wrapper
    ./gradlew clean build -x test --no-daemon
    
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