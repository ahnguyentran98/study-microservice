# API Gateway Docker Setup

This directory contains Docker configuration for running the API Gateway service both standalone and with integrated microservices.

## Prerequisites

- Docker
- Docker Compose
- Java 17+ (for building the application)

## Quick Start

### Option 1: Standalone API Gateway
```bash
./gradlew build
docker-compose up -d
```

### Option 2: API Gateway with User Service (for testing)
```bash
./gradlew build
cd ../user-service && ./gradlew build && cd ../api-gateway
docker-compose -f docker-compose-with-services.yml up -d
```

## Available Configurations

### 1. docker-compose.yml (Standalone)
- **API Gateway only**
- **Port**: 8080
- **Use case**: When other services are running separately or externally

### 2. docker-compose-with-services.yml (Integrated)
- **API Gateway**: Port 8080
- **User Service**: Port 8081  
- **PostgreSQL Database**: Port 5434
- **Use case**: Complete testing environment

## Current Status

✅ **Working:**
- API Gateway builds and starts successfully
- Health endpoints are accessible
- Routes are configured correctly
- Network connectivity to external services

⚠️ **Network Configuration:**
- For full integration testing, use the `docker-compose-with-services.yml` configuration
- The standalone setup requires external services to be running
- Routes are configured to use Docker service names

## Services

### api-gateway
- **Image**: Built from local Dockerfile
- **Port**: 8888 (mapped to 8080 inside container)
- **Profile**: docker
- **Health Check**: http://localhost:8888/actuator/health

### Configuration

The API Gateway uses different service URLs based on the environment:

#### Docker Environment (application-docker.properties)
- **User Service**: http://user-service:8080
- **Product Service**: http://product-service:8080
- **Order Service**: http://order-service:8080
- **Payment Service**: http://payment-service:8080
- **Notification Service**: http://notification-service:8080

#### Local Environment (application.properties)
- **User Service**: http://localhost:8081
- **Product Service**: http://localhost:8082
- **Order Service**: http://localhost:8083
- **Payment Service**: http://localhost:8084
- **Notification Service**: http://localhost:8085

## Useful Commands

### Build and start services
```bash
# Standalone API Gateway
docker-compose up -d

# With User Service for testing
docker-compose -f docker-compose-with-services.yml up -d
```

### Check service status
```bash
docker-compose ps
```

### View logs
```bash
# API Gateway logs
docker-compose logs -f api-gateway

# All services (when using integrated setup)
docker-compose -f docker-compose-with-services.yml logs -f
```

### Stop services
```bash
docker-compose down

# With volumes cleanup
docker-compose down -v
```

### Test the API Gateway

#### Health Check
```bash
curl http://localhost:8888/actuator/health
```

#### Gateway Routes
```bash
# View configured routes
curl http://localhost:8888/actuator/gateway/routes
```

#### Test User Service through Gateway (when using integrated setup)
```bash
# Note: Routing may require additional network configuration
# First ensure both services are on the same network
curl http://localhost:8888/api/users/profile \
  -H "X-User-Email: test@example.com" \
  -H "X-User-Id: 1" \
  -H "X-User-Role: USER"
```

## Environment Variables

The following environment variables can be overridden:

- **USER_SERVICE_URL**: URL for user service (default: http://user-service:8080)
- **PRODUCT_SERVICE_URL**: URL for product service  
- **ORDER_SERVICE_URL**: URL for order service
- **PAYMENT_SERVICE_URL**: URL for payment service
- **NOTIFICATION_SERVICE_URL**: URL for notification service
- **JWT_SECRET**: JWT signing secret
- **JWT_EXPIRATION**: JWT token expiration time in milliseconds

## Gateway Features

### 1. Route Configuration
- **Path-based routing** to microservices
- **Load balancing** (when multiple instances available)
- **Service discovery** integration

### 2. Circuit Breaker
- **Resilience4j** integration
- **Fallback endpoints** for service failures
- **Health indicators** for monitoring

### 3. Security
- **JWT token** validation
- **CORS** configuration
- **Authentication** filters

### 4. Monitoring
- **Actuator endpoints** for health and metrics
- **Gateway-specific endpoints** for route inspection
- **Detailed logging** for debugging

## Troubleshooting

### 1. Gateway cannot reach services
- Ensure services are in the same Docker network
- Check service names match the configured URLs
- Verify services are healthy before gateway starts

### 2. CORS issues
- Check CORS configuration in application-docker.properties
- Verify allowed origins match your frontend domain

### 3. Circuit breaker activation
- Check service health and response times
- Review circuit breaker thresholds in configuration
- Monitor actuator/health endpoint for circuit breaker status

### 4. JWT token issues
- Verify JWT_SECRET matches across all services
- Check token expiration settings
- Ensure proper Authorization header format

## Development Tips

### 1. Hot reload during development
```bash
# Rebuild and restart gateway only
docker-compose up -d --build api-gateway
```

### 2. Testing with external services
```bash
# Use standalone setup and point to external service URLs
docker-compose up -d
```

### 3. Network debugging
```bash
# Access gateway container to test network connectivity
docker exec -it api-gateway-app sh
curl http://user-service:8080/actuator/health
```
