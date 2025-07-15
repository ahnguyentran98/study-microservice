# Microservices E-Commerce Platform - Quick Start Guide

## Prerequisites

1. **Docker & Docker Compose** - Install Docker Desktop
2. **Java 11+** - For local development
3. **Node.js 16+** - For frontend development

## Project Structure

```
study-microservice/
├── api-gateway/           # Spring Cloud Gateway
├── user-service/          # User authentication & profiles
├── product-service/       # Product catalog & search
├── order-service/         # Order management
├── payment-service/       # Payment processing
├── notification-service/  # Email/SMS notifications
├── frontend/              # Vue.js frontend
└── docker-compose.yml     # Docker orchestration
```

## Quick Start

### 1. Start Infrastructure Services

```bash
# Start databases and message queue
docker-compose up -d postgres mongodb redis rabbitmq
```

### 2. Build and Start All Services

```bash
# Start all services
docker-compose up --build

# Or start services individually
docker-compose up --build user-service
docker-compose up --build product-service
docker-compose up --build order-service
docker-compose up --build payment-service
docker-compose up --build notification-service
docker-compose up --build api-gateway
docker-compose up --build frontend
```

### 3. Access the Application

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Individual Services**:
  - User Service: http://localhost:8081
  - Product Service: http://localhost:8082
  - Order Service: http://localhost:8083
  - Payment Service: http://localhost:8084
  - Notification Service: http://localhost:8085

### 4. Access Management UIs

- **RabbitMQ Management**: http://localhost:15672 (admin/password)
- **PostgreSQL**: localhost:5432 (admin/password)
- **MongoDB**: localhost:27017 (admin/password)
- **Redis**: localhost:6379

## API Endpoints

### User Service
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User login
- `GET /api/users/profile` - Get user profile
- `PUT /api/users/profile` - Update user profile

### Product Service
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products/search` - Search products
- `PUT /api/products/{id}/inventory` - Update inventory

### Order Service
- `POST /api/orders` - Create new order
- `GET /api/orders/{userId}` - Get user orders
- `GET /api/orders/{orderId}` - Get order details
- `PUT /api/orders/{orderId}/status` - Update order status

### Payment Service
- `POST /api/payments/process` - Process payment
- `POST /api/payments/refund` - Refund payment
- `GET /api/payments/{orderId}` - Get payment details

### Notification Service
- `POST /api/notifications/email` - Send email
- `POST /api/notifications/sms` - Send SMS
- `GET /api/notifications/{userId}/history` - Get notification history

## Development Workflow

### 1. Local Development

```bash
# Start infrastructure
docker-compose up -d postgres mongodb redis rabbitmq

# Run services locally with your IDE
# Each service runs on its designated port
```

### 2. Frontend Development

```bash
cd frontend
npm install
npm run serve
```

### 3. Testing

```bash
# Test individual service
curl http://localhost:8080/api/users/profile

# Test through API Gateway
curl http://localhost:8080/api/products
```

## Troubleshooting

### Common Issues

1. **Port Conflicts**: Make sure ports 3000, 5432, 6379, 8080-8085 are available
2. **Docker Issues**: Restart Docker Desktop if containers won't start
3. **Database Connection**: Wait for databases to fully initialize before starting services

### Service Dependencies

```
Frontend → API Gateway → Individual Services → Databases
```

### Build Issues

If Gradle wrapper issues occur:

```bash
# Use system gradle if available
gradle wrapper

# Or use Docker builds exclusively
docker-compose build
```

## Next Steps

1. **Add Authentication**: Implement JWT tokens for secure API access
2. **Add Monitoring**: Set up Prometheus/Grafana for metrics
3. **Add Logging**: Implement centralized logging with ELK stack
4. **Add Testing**: Write unit and integration tests
5. **Add CI/CD**: Set up automated builds and deployments

## Architecture Features

✅ **Microservices Architecture**: Each service is independent and scalable
✅ **API Gateway**: Single entry point for all client requests
✅ **Service Communication**: REST APIs and async messaging
✅ **Database per Service**: Each service has its own database
✅ **Event-Driven**: Asynchronous communication via RabbitMQ
✅ **Containerization**: Docker containers for consistent deployment
✅ **Modern Frontend**: Vue.js 3 with Composition API and Pinia
✅ **Responsive UI**: Vuetify components for professional appearance

This implementation provides a solid foundation for learning microservices architecture with practical e-commerce use cases!