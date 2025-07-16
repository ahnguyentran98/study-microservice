# API Gateway - Microservices Architecture

This API Gateway serves as the single entry point for all microservices in the e-commerce platform, providing routing, load balancing, security, and monitoring capabilities.

## 🏗️ Architecture Overview

The API Gateway is built with:
- **Spring Boot 3.2.0** - Main framework
- **Spring Cloud Gateway** - Reactive gateway framework
- **Spring WebFlux** - Reactive web framework
- **Resilience4J** - Circuit breaker and resilience patterns
- **Java 17** - Runtime environment
- **Gradle 8.5** - Build tool

## 🚀 Key Features

### 🔀 Request Routing
- **Path-based routing** to appropriate microservices
- **Method-based routing** for specific HTTP methods
- **Load balancing** across service instances
- **Service discovery** integration

### 🛡️ Security & Authorization

### 🛡️ Security & Authorization
- **Authorization** is handled by the API Gateway for all protected endpoints
- **Authentication** is delegated to the User Service
- **Role-based access control** (USER/ADMIN)
- **User-specific resource protection**
- **Centralized authorization** for all microservices
- **CORS configuration** for frontend integration
- **Request/Response filtering** for security headers
- **Request ID tracking** for distributed tracing


### Authentication & Authorization Separation
- **Authentication** (verifying user identity and JWT token validation) is performed by the User Service.
- **Authorization** (access control, role validation, resource protection) is enforced by the API Gateway.

#### JWT Token Validation
- The API Gateway receives JWT tokens issued by the User Service and validates them for authorization purposes.

#### Role-Based Access Control
- Admin endpoints are restricted to ADMIN role (authorization by API Gateway)
- User-specific endpoints are protected by ownership validation (authorization by API Gateway)
- Public endpoints (registration, login, health checks) do not require authentication or authorization

#### Authorization Rules
- Admin access: Full access to all endpoints (authorization by API Gateway)
- User access: Limited to own profile and orders (authorization by API Gateway)
- Public access: Registration, login, health checks (no authentication required)

#### Request Filtering
- Request ID injection for tracing
- Header sanitization for security
- CORS handling for frontend integration
- User context injection via headers (X-User-Email, X-User-Id, X-User-Role)

#### Circuit Breaker Protection
- Automatic failure detection
- Fallback responses when services are down
- Gradual service recovery
- **Timeout handling** for slow services

### 📊 Monitoring & Logging
- **Request/Response logging** with timing
- **Health checks** for all services
- **Actuator endpoints** for metrics
- **Distributed tracing** support

## 📂 Project Structure

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/microservices/apigateway/
│   │   │   ├── config/
│   │   │   │   ├── GatewayConfig.java       # Route configuration
│   │   │   │   └── CorsConfig.java          # CORS configuration
│   │   │   ├── controller/
│   │   │   │   ├── FallbackController.java  # Circuit breaker fallbacks
│   │   │   │   └── HealthController.java    # Health check endpoints
│   │   │   ├── filter/
│   │   │   │   ├── LoggingGlobalFilter.java # Request logging filter
│   │   │   │   └── JwtAuthenticationFilter.java # JWT authorization filter
│   │   │   ├── security/
│   │   │   │   └── JwtUtils.java            # JWT token validation
│   │   │   └── ApiGatewayApplication.java   # Main application class
│   │   └── resources/
│   │       └── application.properties       # Configuration
├── build.gradle                             # Build configuration
├── Dockerfile                               # Container configuration
└── README.md                                # This file
```

## 🔗 Service Routes

### User Service (Port 8081)
- `POST /api/users/register` - User registration
- `POST /api/users/login` - User login
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/{id}` - Get user by ID (admin)
- `DELETE /api/users/{id}` - Delete user (admin)

### Product Service (Port 8082)
- `GET /api/products` - List all products
- `POST /api/products` - Create new product
- `GET /api/products/{id}` - Get product by ID
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product
- `POST /api/products/search` - Search products
- `PUT /api/products/{id}/inventory` - Update inventory

### Order Service (Port 8083)
- `POST /api/orders` - Create new order
- `GET /api/orders/user/{userId}` - Get user orders
- `GET /api/orders/{orderId}` - Get order by ID
- `PUT /api/orders/{orderId}/status` - Update order status

### Payment Service (Port 8084)
- `POST /api/payments/process` - Process payment
- `POST /api/payments/refund` - Process refund
- `GET /api/payments/order/{orderId}` - Get payment by order

### Notification Service (Port 8085)
- `POST /api/notifications/email` - Send email notification
- `POST /api/notifications/sms` - Send SMS notification
- `GET /api/notifications/user/{userId}/history` - Get notification history

## 🛠️ Prerequisites

- **Java 17+** (required for Spring Boot 3)
- **Gradle 8.5+** (compatible with Spring Boot 3)
- **Docker** (optional, for containerization)

## 🚀 Running the Gateway

### 1. **Build the Service**
```bash
./gradlew build
```

### 2. **Run the Service**
```bash
# Option 1: Using Gradle
./gradlew bootRun

# Option 2: Using JAR
java -jar build/libs/api-gateway-1.0.0.jar

# Option 3: Using Docker
docker build -t api-gateway .
docker run -p 8080:8080 api-gateway
```

### 3. **Verify Gateway is Running**
```bash
# Health check
curl http://localhost:8080/health

# Gateway info
curl http://localhost:8080/info

# Actuator endpoints
curl http://localhost:8080/actuator/gateway/routes
```

## 🔧 Configuration

### Circuit Breaker Settings
```yaml
resilience4j:
  circuitbreaker:
    instances:
      user-service:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
```

### Rate Limiting
```yaml
filters:
  - name: RequestRateLimiter
    args:
      redis-rate-limiter.replenishRate: 10
      redis-rate-limiter.burstCapacity: 20
```

### CORS Configuration
```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOrigins: "*"
      allowedMethods: [GET, POST, PUT, DELETE, OPTIONS]
      allowedHeaders: "*"
      allowCredentials: true
```

## 📊 Monitoring & Health Checks

### Health Endpoints
```bash
# Gateway health
GET /health

# Service info
GET /info

# All routes
GET /actuator/gateway/routes

# Circuit breaker metrics
GET /actuator/metrics
```

### Logging
The gateway logs all requests with:
- Request ID for tracing
- Method and URI
- Response status and duration
- Error details if any

Example log:
```
🔵 [REQUEST] 2024-01-15T10:30:00 | POST /api/users/login | Request ID: abc-123
🟢 [RESPONSE] 2024-01-15T10:30:01 | POST /api/users/login | Status: 200 | Duration: 245ms
```

## 🛡️ Security & Authorization

### JWT Token Validation
- **Token extraction** from Authorization header
- **Token signature verification** using shared secret
- **Token expiration validation**
- **Claims extraction** (email, userId, role)

### Role-Based Access Control
- **Admin endpoints** restricted to ADMIN role
- **User-specific endpoints** with ownership validation
- **Public endpoints** accessible without authentication

### Authorization Rules
- **Admin access**: Full access to all endpoints
- **User access**: Limited to own profile and orders
- **Public access**: Registration, login, health checks

### Request Filtering
- **Request ID injection** for tracing
- **Header sanitization** for security
- **CORS handling** for frontend integration
- **User context injection** via headers (X-User-Email, X-User-Id, X-User-Role)

### Circuit Breaker Protection
- **Automatic failure detection**
- **Fallback responses** when services are down
- **Gradual service recovery**

## 🔄 Fallback Responses

When services are unavailable, the gateway returns structured error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 503,
  "error": "Service Unavailable",
  "message": "Service is temporarily unavailable. Please try again later.",
  "path": "/api/users/login",
  "service": "user-service"
}
```

## 🐳 Docker Integration

### Build Image
```bash
docker build -t api-gateway .
```

### Run Container
```bash
docker run -p 8080:8080 \
  --name api-gateway \
  -e SPRING_PROFILES_ACTIVE=docker \
  api-gateway
```

### Health Check
The Docker container includes health checks:
```bash
docker ps  # Check container health status
```

## 🚨 Troubleshooting

### Common Issues

1. **Service Connection Failed**
   ```bash
   # Check if target services are running
   curl http://localhost:8081/actuator/health  # User service
   curl http://localhost:8082/actuator/health  # Product service
   ```

2. **CORS Issues**
   ```bash
   # Check CORS configuration
   curl -X OPTIONS http://localhost:8080/api/users/login \
     -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST"
   ```

3. **Circuit Breaker Triggered**
   ```bash
   # Check circuit breaker status
   curl http://localhost:8080/actuator/circuitbreakers
   ```

4. **Rate Limiting Activated**
   ```bash
   # Check rate limiter metrics
   curl http://localhost:8080/actuator/metrics/gateway.requests
   ```

## 📈 Performance Tuning

### JVM Options
```bash
java -Xms512m -Xmx1024m -jar api-gateway.jar
```

### Connection Pooling
```yaml
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-connections: 500
          max-idle-time: 30s
```

## 🔧 Development

### Adding New Routes
1. Add route configuration in `application.yml`
2. Update `GatewayConfig.java` if needed
3. Add fallback endpoint in `FallbackController.java`
4. Update circuit breaker configuration

### Testing Routes
```bash
# Test user service route
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Test with invalid service (should trigger fallback)
curl http://localhost:8080/api/nonexistent/test
```

## 📚 Integration with Other Services

The API Gateway is designed to work seamlessly with:
- **User Service** - Authentication and user management
- **Product Service** - Product catalog and inventory
- **Order Service** - Order processing and management
- **Payment Service** - Payment processing
- **Notification Service** - Email and SMS notifications
- **Frontend Application** - Vue.js single-page application

## 🎯 Best Practices

1. **Always use circuit breakers** for external service calls
2. **Implement proper logging** for debugging and monitoring
3. **Configure rate limiting** to prevent abuse
4. **Use health checks** for service discovery
5. **Handle CORS properly** for frontend integration
6. **Monitor performance** with actuator endpoints
7. **Implement graceful degradation** with fallbacks

This API Gateway provides a robust, scalable, and secure entry point for your microservices architecture.