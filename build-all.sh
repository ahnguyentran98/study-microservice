#!/bin/bash

echo "Building all microservices..."

echo "Config gradle version to 7.6.1"
sudo apt update

sudo apt install zip unzip curl

curl -s "https://get.sdkman.io" | bash

source "$HOME/.sdkman/bin/sdkman-init.sh"

sdk install gradle 7.6.1

sdk use gradle 7.6.1

echo "Config gradle version to 7.6.1 done"
# Stop any existing Gradle daemons
echo "Stop all gradle daemons"
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