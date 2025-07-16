#!/bin/bash

echo "Building all microservices..."

# Stop any existing Gradle daemons
gradle --stop

# Build Java services
services=("user-service" "product-service" "order-service" "payment-service" "notification-service" "api-gateway")

for service in "${services[@]}"; do
    echo "================================================="
    echo "Building $service..."
    
    if [ ! -d "$service" ]; then
        echo "❌ Directory $service not found, skipping..."
        continue
    fi
    
    cd "$service"
    
    # Check if wrapper exists and works
    if [ ! -f "gradlew" ] || ! ./gradlew --version > /dev/null 2>&1; then
        echo "🔧 Fixing Gradle wrapper for $service..."
        
        # Clean old wrapper
        rm -rf gradle/ gradlew gradlew.bat gradle.properties
        
        # Generate new wrapper
        gradle wrapper --gradle-version 7.6.1
        chmod +x gradlew
        
        # Verify wrapper works
        if ! ./gradlew --version > /dev/null 2>&1; then
            echo "❌ Failed to fix wrapper for $service"
            cd ..
            continue
        fi
        echo "✅ Wrapper fixed for $service"
    fi
    
    # Build the service
    echo "🏗️  Building $service..."
    ./gradlew clean build -x test --no-daemon --stacktrace
    
    if [ $? -eq 0 ]; then
        echo "✅ $service built successfully"
    else
        echo "❌ $service build failed"
        echo "Check the error above for details"
        cd ..
        exit 1
    fi
    
    cd ..
done

echo "================================================="
echo "All services built successfully!"
echo "Run 'docker-compose up --build' to start all services"