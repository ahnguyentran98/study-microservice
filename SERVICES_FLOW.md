# 🔄 Microservices Communication Flow & Mechanisms
## E-Commerce Platform - Java Backend + Vue.js Frontend

### 🎯 **Architecture Overview**
This document details the communication patterns and service flows for our e-commerce microservices platform built with:
- **Backend**: Spring Boot 3.2+ (Jakarta EE 9+) with Java 17+
- **Frontend**: Vue.js 3 + Composition API
- **Infrastructure**: Docker Compose for local development
- **Databases**: PostgreSQL, MongoDB, Redis, Elasticsearch
- **Messaging**: RabbitMQ for event-driven communication

## 📋 **Table of Contents**
1. [Service Communication Patterns](#service-communication-patterns)
2. [Request Flow Examples](#request-flow-examples)
3. [Service Discovery & Routing](#service-discovery--routing)
4. [Event-Driven Architecture](#event-driven-architecture)
5. [Database Management](#database-management)
6. [Logging & Monitoring](#logging--monitoring)
7. [Error Handling & Resilience](#error-handling--resilience)
8. [Security & Authentication](#security--authentication)
9. [Troubleshooting Guide](#troubleshooting-guide)

---

## 🔗 **Service Communication Patterns**

### **Local Development Architecture**
```
Your Machine:
├── Frontend (Vue.js)     → localhost:3000
├── API Gateway           → localhost:8080
├── User Service         → localhost:8081
├── Product Service      → localhost:8082
├── Order Service        → localhost:8083
├── Payment Service      → localhost:8084
├── Notification Service → localhost:8085
├── PostgreSQL           → localhost:5432
├── MongoDB              → localhost:27017
├── Redis                → localhost:6379
└── RabbitMQ            → localhost:5672 (Management: 15672)
```

### **1. Synchronous Communication (REST APIs)**

#### **Frontend → API Gateway → Services**
```mermaid
graph LR
    A[Frontend Vue.js :3000] --> B[API Gateway :8080]
    B --> C[User Service :8081]
    B --> D[Product Service :8082]
    B --> E[Order Service :8083]
    B --> F[Payment Service :8084]
    B --> G[Notification Service :8085]
```

#### **Service Specifications**

**User Service (Spring Boot 3.2 + Jakarta EE 9)**
- **Purpose**: Handle user authentication, profiles, preferences
- **Tech Stack**: Spring Boot 3.2 + JWT + PostgreSQL
- **Database**: PostgreSQL (structured user data)
- **Endpoints**:
  - `POST /api/users/register`
  - `POST /api/users/login`
  - `GET /api/users/profile`
  - `PUT /api/users/profile`

**Product Service**
- **Purpose**: Manage product catalog, inventory, search
- **Tech Stack**: Spring Boot 3.2 + Elasticsearch + PostgreSQL + Redis
- **Database**: PostgreSQL + Elasticsearch (search optimization) + Redis (caching)
- **Endpoints**:
  - `GET /api/products`
  - `GET /api/products/{id}`
  - `POST /api/products/search`
  - `PUT /api/products/{id}/inventory`

**Order Service**
- **Purpose**: Handle order processing, order history
- **Tech Stack**: Spring Boot 3.2 + PostgreSQL + Redis + RabbitMQ
- **Database**: PostgreSQL + Redis (fast lookups)
- **Endpoints**:
  - `POST /api/orders`
  - `GET /api/orders/{userId}`
  - `GET /api/orders/{orderId}`
  - `PUT /api/orders/{orderId}/status`

**Payment Service**
- **Purpose**: Process payments, handle refunds
- **Tech Stack**: Spring Boot 3.2 + PostgreSQL + Stripe API
- **Database**: PostgreSQL (transaction integrity)
- **Endpoints**:
  - `POST /api/payments/process`
  - `POST /api/payments/refund`
  - `GET /api/payments/{orderId}`

**Notification Service**
- **Purpose**: Send emails, SMS, push notifications
- **Tech Stack**: Spring Boot 3.2 + MongoDB + RabbitMQ
- **Database**: MongoDB (flexible message formats)
- **Endpoints**:
  - `POST /api/notifications/email`
  - `POST /api/notifications/sms`
  - `GET /api/notifications/{userId}/history`

#### **API Gateway Routing Configuration (Spring Cloud Gateway)**
```java
// GatewayConfig.java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r.path("/api/users/**")
                    .uri("http://user-service:8080"))
            .route("product-service", r -> r.path("/api/products/**")
                    .uri("http://product-service:8080"))
            .route("order-service", r -> r.path("/api/orders/**")
                    .uri("http://order-service:8080"))
            .route("payment-service", r -> r.path("/api/payments/**")
                    .uri("http://payment-service:8080"))
            .route("notification-service", r -> r.path("/api/notifications/**")
                    .uri("http://notification-service:8080"))
            .build();
    }
}
```

#### **How Request Routing Works**
1. **Frontend** sends HTTP request to `http://localhost:3000/api/users/profile`
2. **Vue.js Router** routes internally, then makes API call to API Gateway
3. **API Gateway** (localhost:8080) receives request at `/api/users/profile`
4. **Spring Cloud Gateway** matches `/api/users/**` pattern
5. **Routes** request to `http://user-service:8080/api/users/profile`
6. **User Service** processes request with Spring Boot 3.2 + Jakarta EE
7. **Response** flows back through the same path

### **2. Service-to-Service Communication (Spring Boot 3.2)**

#### **WebClient Configuration (Modern Spring Boot 3.2)**
```java
// WebClientConfig.java in Order Service
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }
    
    @Bean
    public WebClient userServiceClient(WebClient.Builder builder) {
        return builder.baseUrl("http://user-service:8080").build();
    }
    
    @Bean
    public WebClient productServiceClient(WebClient.Builder builder) {
        return builder.baseUrl("http://product-service:8080").build();
    }
    
    @Bean 
    public WebClient paymentServiceClient(WebClient.Builder builder) {
        return builder.baseUrl("http://payment-service:8080").build();
    }
}
```

#### **Service Call Example (Spring Boot 3.2 + Jakarta EE)**
```java
// OrderService.java
@Service
public class OrderService {
    
    private final WebClient userServiceClient;
    private final WebClient productServiceClient;
    private final WebClient paymentServiceClient;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;
    
    public Order createOrder(OrderRequest request) {
        // 1. Validate user exists
        User user = userServiceClient
            .get()
            .uri("/api/users/{id}", request.getUserId())
            .retrieve()
            .bodyToMono(User.class)
            .timeout(Duration.ofSeconds(5))
            .block();
            
        if (user == null) {
            throw new UserNotFoundException("User not found: " + request.getUserId());
        }
            
        // 2. Validate products and check inventory
        List<Product> products = new ArrayList<>();
        for (OrderItemRequest item : request.getItems()) {
            Product product = productServiceClient
                .get()
                .uri("/api/products/{id}", item.getProductId())
                .retrieve()
                .bodyToMono(Product.class)
                .timeout(Duration.ofSeconds(5))
                .block();
                
            if (product == null) {
                throw new ProductNotFoundException("Product not found: " + item.getProductId());
            }
                
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for " + product.getName());
            }
            products.add(product);
        }
        
        // 3. Create order entity
        Order order = new Order();
        order.setUserId(user.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(calculateTotal(request.getItems(), products));
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(mapOrderItems(request.getItems(), products));
        
        // 4. Save order to PostgreSQL
        Order savedOrder = orderRepository.save(order);
        
        // 5. Update inventory (async call)
        updateInventoryAsync(request.getItems());
        
        // 6. Publish order created event to RabbitMQ
        orderEventPublisher.publishOrderCreated(savedOrder);
        
        return savedOrder;
    }
    
    private void updateInventoryAsync(List<OrderItemRequest> items) {
        CompletableFuture.runAsync(() -> {
            for (OrderItemRequest item : items) {
                productServiceClient
                    .put()
                    .uri("/api/products/{id}/inventory", item.getProductId())
                    .bodyValue(Map.of("quantity", -item.getQuantity()))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .subscribe();
            }
        });
    }
}
```

---

## 🔄 **Request Flow Examples**

### **Example 1: User Registration Flow (Spring Boot 3.2 + JWT)**

```mermaid
sequenceDiagram
    participant F as Frontend Vue.js
    participant G as API Gateway
    participant U as User Service
    participant DB as PostgreSQL
    
    F->>G: POST /api/users/register
    Note over F: Vue.js form submission
    G->>U: Forward registration request
    U->>U: Validate user data (Jakarta Bean Validation)
    U->>U: Hash password (BCrypt)
    U->>DB: Save user to PostgreSQL
    DB-->>U: User entity saved
    U->>U: Generate JWT token (Spring Security 6.x)
    U-->>G: Return user + JWT token
    G-->>F: Return response with token
    F->>F: Store token in Pinia store
    F->>F: Redirect to dashboard
```

### **Example 2: Product Search Flow (with Redis Caching + Elasticsearch)**

```mermaid
sequenceDiagram
    participant F as Frontend Vue.js
    participant G as API Gateway
    participant P as Product Service
    participant R as Redis Cache
    participant E as Elasticsearch
    participant DB as PostgreSQL
    
    F->>G: POST /api/products/search
    Note over F: Vue.js search component
    G->>P: Forward search request
    P->>R: Check Redis cache for search results
    alt Cache Hit
        R-->>P: Return cached search results
        P-->>G: Return cached products
    else Cache Miss
        P->>E: Search products in Elasticsearch
        E-->>P: Return search results
        P->>DB: Get full product details from PostgreSQL
        DB-->>P: Return product entities
        P->>R: Cache search results (TTL: 15 minutes)
        P-->>G: Return enriched products
    end
    G-->>F: Return search response
    F->>F: Update Vue.js component with results
```

### **Example 3: Complete Order Creation Flow (Event-Driven with RabbitMQ)**

```mermaid
sequenceDiagram
    participant F as Frontend Vue.js
    participant G as API Gateway
    participant O as Order Service
    participant U as User Service
    participant P as Product Service
    participant Pay as Payment Service
    participant N as Notification Service
    participant MQ as RabbitMQ
    participant PG as PostgreSQL
    participant MDB as MongoDB
    
    F->>G: POST /api/orders
    Note over F: Vue.js checkout process
    G->>O: Forward order creation request
    
    # Synchronous validation phase
    O->>U: GET /api/users/{userId}
    U->>PG: Query user from PostgreSQL
    PG-->>U: Return user entity
    U-->>O: User validation successful
    
    O->>P: GET /api/products/{productId} (for each item)
    P->>PG: Query product inventory
    PG-->>P: Return product with stock info
    P-->>O: Product validation + inventory check
    
    # Order creation phase
    O->>PG: Save order to PostgreSQL
    PG-->>O: Order entity saved with ID
    
    O->>P: PUT /api/products/{id}/inventory (update stock)
    P->>PG: Update product inventory
    
    # Asynchronous event-driven phase
    O->>MQ: Publish OrderCreated event
    Note over MQ: Event routing to multiple consumers
    
    par Payment Processing
        MQ->>Pay: OrderCreated event
        Pay->>Pay: Process payment (Stripe API)
        Pay->>MQ: Publish PaymentProcessed event
        MQ->>O: Update order status to PAID
        O->>PG: Update order status
    and Notification
        MQ->>N: OrderCreated event
        N->>MDB: Log notification to MongoDB
        N->>N: Send email confirmation
        N->>MQ: Publish NotificationSent event
    end
    
    O-->>G: Return order confirmation
    G-->>F: Return order response
    F->>F: Update Vue.js state (Pinia store)
    F->>F: Redirect to order confirmation page
```

---

## 🎯 **Service Discovery & Routing**

### **Docker Compose Network Configuration (README Aligned)**

#### **Complete Docker Compose Setup**
```yaml
# docker-compose.yml (Based on README specifications)
version: '3.8'
services:
  # Databases
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: microservices_db
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - microservices-network

  mongodb:
    image: mongo:5.0
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: password
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    networks:
      - microservices-network

  redis:
    image: redis:6.2
    ports:
      - "6379:6379"
    networks:
      - microservices-network

  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: password
    ports:
      - "5672:5672"
      - "15672:15672"  # Management UI
    networks:
      - microservices-network

  # Microservices (Spring Boot 3.2 + Java 17)
  user-service:
    build: ./user-service
    ports:
      - "8081:8080"
    depends_on:
      - postgres
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/microservices_db
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=password
    networks:
      - microservices-network

  product-service:
    build: ./product-service
    ports:
      - "8082:8080"
    depends_on:
      - postgres
      - redis
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/microservices_db
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    networks:
      - microservices-network

  order-service:
    build: ./order-service
    ports:
      - "8083:8080"
    depends_on:
      - postgres
      - rabbitmq
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/microservices_db
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_USERNAME=admin
      - SPRING_RABBITMQ_PASSWORD=password
    networks:
      - microservices-network

  payment-service:
    build: ./payment-service
    ports:
      - "8084:8080"
    depends_on:
      - postgres
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/microservices_db
    networks:
      - microservices-network

  notification-service:
    build: ./notification-service
    ports:
      - "8085:8080"
    depends_on:
      - mongodb
      - rabbitmq
    environment:
      - SPRING_DATA_MONGODB_URI=mongodb://admin:password@mongodb:27017/notifications
      - SPRING_RABBITMQ_HOST=rabbitmq
    networks:
      - microservices-network

  # API Gateway (Spring Cloud Gateway)
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    depends_on:
      - user-service
      - product-service
      - order-service
      - payment-service
      - notification-service
    networks:
      - microservices-network

  # Frontend (Vue.js 3)
  frontend:
    build: ./frontend
    ports:
      - "3000:3000"
    depends_on:
      - api-gateway
    networks:
      - microservices-network

networks:
  microservices-network:
    driver: bridge

volumes:
  postgres_data:
  mongodb_data:
```

#### **Service Resolution Process**
1. **DNS Resolution**: Docker's embedded DNS server resolves service names
2. **Network Discovery**: Services find each other by container names within `microservices-network`
3. **Load Balancing**: Docker handles load balancing between service instances
4. **Health Checks**: Services can check each other's health via Actuator endpoints

#### **Quick Start Commands (README Aligned)**
```bash
# Start all services
docker-compose up -d

# View logs for all services
docker-compose logs -f

# View logs for specific service
docker-compose logs -f user-service

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose down && docker-compose up --build -d

# Check service health
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Order Service
```

### **API Gateway Service Discovery (Spring Cloud Gateway)**
```yaml
# application.yml in API Gateway
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: http://user-service:8080
          predicates:
            - Path=/api/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: user-service
                fallbackUri: forward:/fallback/user-service
        - id: product-service
          uri: http://product-service:8080
          predicates:
            - Path=/api/products/**
          filters:
            - name: CircuitBreaker
              args:
                name: product-service
                fallbackUri: forward:/fallback/product-service
        - id: order-service
          uri: http://order-service:8080
          predicates:
            - Path=/api/orders/**
          filters:
            - name: CircuitBreaker
              args:
                name: order-service
                fallbackUri: forward:/fallback/order-service
        - id: payment-service
          uri: http://payment-service:8080
          predicates:
            - Path=/api/payments/**
          filters:
            - name: CircuitBreaker
              args:
                name: payment-service
                fallbackUri: forward:/fallback/payment-service
        - id: notification-service
          uri: http://notification-service:8080
          predicates:
            - Path=/api/notifications/**
          filters:
            - name: CircuitBreaker
              args:
                name: notification-service
                fallbackUri: forward:/fallback/notification-service
```

---

## 🌐 **Frontend Architecture (Vue.js 3)**

### **Single Page Application Structure (README Aligned)**
```
frontend/
├── src/
│   ├── components/
│   │   ├── user/
│   │   │   ├── LoginForm.vue
│   │   │   ├── RegisterForm.vue
│   │   │   └── UserProfile.vue
│   │   ├── products/
│   │   │   ├── ProductList.vue
│   │   │   ├── ProductCard.vue
│   │   │   └── ProductSearch.vue
│   │   ├── orders/
│   │   │   ├── OrderForm.vue
│   │   │   ├── OrderHistory.vue
│   │   │   └── OrderDetails.vue
│   │   └── shared/
│   │       ├── Header.vue
│   │       ├── Footer.vue
│   │       └── LoadingSpinner.vue
│   ├── services/
│   │   ├── userService.js
│   │   ├── productService.js
│   │   ├── orderService.js
│   │   ├── paymentService.js
│   │   └── apiClient.js
│   ├── stores/ (Pinia)
│   │   ├── userStore.js
│   │   ├── productStore.js
│   │   ├── orderStore.js
│   │   └── cartStore.js
│   ├── router/
│   │   └── index.js
│   ├── views/
│   │   ├── Home.vue
│   │   ├── Login.vue
│   │   ├── Products.vue
│   │   ├── Cart.vue
│   │   └── Orders.vue
│   └── utils/
│       ├── auth.js
│       └── constants.js
├── public/
├── package.json
└── vite.config.js
```

### **API Integration Pattern (README Aligned)**
```javascript
// apiClient.js
import axios from 'axios'

const BASE_URL = 'http://localhost:8080' // API Gateway

const BASE_URLS = {
  user: `${BASE_URL}/api/users`,
  product: `${BASE_URL}/api/products`,
  order: `${BASE_URL}/api/orders`,
  payment: `${BASE_URL}/api/payments`,
  notification: `${BASE_URL}/api/notifications`
}

// Create axios instance with interceptors
const apiClient = axios.create({
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Request interceptor to add JWT token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('jwt_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for error handling
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export { apiClient, BASE_URLS }
```

### **Service Layer Implementation**
```javascript
// userService.js
import { apiClient, BASE_URLS } from './apiClient'

export const userService = {
  async register(userData) {
    const response = await apiClient.post(`${BASE_URLS.user}/register`, userData)
    return response.data
  },

  async login(credentials) {
    const response = await apiClient.post(`${BASE_URLS.user}/login`, credentials)
    if (response.data.token) {
      localStorage.setItem('jwt_token', response.data.token)
    }
    return response.data
  },

  async getProfile() {
    const response = await apiClient.get(`${BASE_URLS.user}/profile`)
    return response.data
  },

  async updateProfile(profileData) {
    const response = await apiClient.put(`${BASE_URLS.user}/profile`, profileData)
    return response.data
  },

  logout() {
    localStorage.removeItem('jwt_token')
  }
}

// productService.js
export const productService = {
  async getProducts(page = 0, size = 20) {
    const response = await apiClient.get(`${BASE_URLS.product}?page=${page}&size=${size}`)
    return response.data
  },

  async searchProducts(searchQuery) {
    const response = await apiClient.post(`${BASE_URLS.product}/search`, searchQuery)
    return response.data
  },

  async getProduct(id) {
    const response = await apiClient.get(`${BASE_URLS.product}/${id}`)
    return response.data
  }
}

// orderService.js
export const orderService = {
  async createOrder(orderData) {
    const response = await apiClient.post(`${BASE_URLS.order}`, orderData)
    return response.data
  },

  async getUserOrders(userId) {
    const response = await apiClient.get(`${BASE_URLS.order}/${userId}`)
    return response.data
  },

  async getOrder(orderId) {
    const response = await apiClient.get(`${BASE_URLS.order}/${orderId}`)
    return response.data
  }
}
```

### **State Management with Pinia**
```javascript
// stores/userStore.js
import { defineStore } from 'pinia'
import { userService } from '@/services/userService'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    isAuthenticated: false,
    loading: false,
    error: null
  }),

  getters: {
    userFullName: (state) => {
      return state.currentUser 
        ? `${state.currentUser.firstName} ${state.currentUser.lastName}`
        : ''
    }
  },

  actions: {
    async login(credentials) {
      this.loading = true
      this.error = null
      try {
        const response = await userService.login(credentials)
        this.currentUser = response.user
        this.isAuthenticated = true
        return response
      } catch (error) {
        this.error = error.response?.data?.message || 'Login failed'
        throw error
      } finally {
        this.loading = false
      }
    },

    async fetchProfile() {
      if (!this.isAuthenticated) return
      
      try {
        this.currentUser = await userService.getProfile()
      } catch (error) {
        this.logout()
        throw error
      }
    },

    logout() {
      userService.logout()
      this.currentUser = null
      this.isAuthenticated = false
      this.error = null
    }
  }
})
```

---

## 📨 **Event-Driven Architecture**

### **RabbitMQ Message Flow**

#### **Message Producer Configuration**
```java
// RabbitMQConfig.java in Order Service
@Configuration
@EnableRabbit
public class RabbitMQConfig {
    
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }
    
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE).build();
    }
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }
    
    @Bean
    public Binding paymentBinding() {
        return BindingBuilder
            .bind(paymentQueue())
            .to(orderExchange())
            .with("order.created");
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(orderExchange())
            .with("order.*");
    }
}
```

#### **Event Publisher**
```java
// OrderEventPublisher.java
@Component
public class OrderEventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
            order.getId(),
            order.getUserId(),
            order.getTotalAmount(),
            order.getItems()
        );
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            "order.created",
            event
        );
        
        log.info("Published OrderCreated event for order: {}", order.getId());
    }
    
    public void publishOrderStatusChanged(Order order, OrderStatus oldStatus) {
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(
            order.getId(),
            oldStatus,
            order.getStatus()
        );
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ORDER_EXCHANGE,
            "order.status.changed",
            event
        );
        
        log.info("Published OrderStatusChanged event for order: {}", order.getId());
    }
}
```

#### **Event Consumer**
```java
// PaymentEventListener.java in Payment Service
@Component
public class PaymentEventListener {
    
    private final PaymentService paymentService;
    
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreated event for order: {}", event.getOrderId());
        
        try {
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setOrderId(event.getOrderId());
            paymentRequest.setUserId(event.getUserId());
            paymentRequest.setAmount(event.getTotalAmount());
            
            Payment payment = paymentService.processPayment(paymentRequest);
            
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                publishPaymentSuccess(event.getOrderId());
            } else {
                publishPaymentFailed(event.getOrderId(), payment.getFailureReason());
            }
            
        } catch (Exception e) {
            log.error("Failed to process payment for order: {}", event.getOrderId(), e);
            publishPaymentFailed(event.getOrderId(), e.getMessage());
        }
    }
}
```

### **Event Flow Diagram**
```mermaid
graph TD
    A[Order Service] -->|OrderCreated| B[RabbitMQ Exchange]
    B -->|order.created| C[Payment Queue]
    B -->|order.created| D[Notification Queue]
    C --> E[Payment Service]
    D --> F[Notification Service]
    E -->|PaymentProcessed| B
    F -->|Send Email| G[Email Provider]
    B -->|payment.processed| H[Order Service]
    H --> I[Update Order Status]
```

---

## 🗄️ **Database Management**

### **Database per Service Pattern (README Aligned)**

#### **Service-Specific Database Configuration**

**User Service - PostgreSQL Configuration**
```yaml
# User Service - application.yml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://postgres:5432/microservices_db
    username: admin
    password: password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    properties:
      hibernate:
        format_sql: true
  security:
    jwt:
      secret: mySecretKey
      expiration: 86400000
```

**Product Service - PostgreSQL + Redis + Elasticsearch**
```yaml
# Product Service - application.yml
spring:
  application:
    name: product-service
  datasource:
    url: jdbc:postgresql://postgres:5432/microservices_db
    username: admin
    password: password
  jpa:
    hibernate:
      ddl-auto: update
  data:
    redis:
      host: redis
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
  elasticsearch:
    uris: http://elasticsearch:9200
    username: elastic
    password: changeme
```

**Order Service - PostgreSQL + Redis + RabbitMQ**
```yaml
# Order Service - application.yml
spring:
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://postgres:5432/microservices_db
    username: admin
    password: password
  jpa:
    hibernate:
      ddl-auto: update
  data:
    redis:
      host: redis
      port: 6379
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: admin
    password: password
```

**Payment Service - PostgreSQL**
```yaml
# Payment Service - application.yml
spring:
  application:
    name: payment-service
  datasource:
    url: jdbc:postgresql://postgres:5432/microservices_db
    username: admin
    password: password
  jpa:
    hibernate:
      ddl-auto: update
  stripe:
    api-key: ${STRIPE_SECRET_KEY:sk_test_...}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_...}
```

**Notification Service - MongoDB + RabbitMQ**
```yaml
# Notification Service - application.yml
spring:
  application:
    name: notification-service
  data:
    mongodb:
      uri: mongodb://admin:password@mongodb:27017/notifications
      database: notifications
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: admin
    password: password
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
```

#### **Database Schema Management (Spring Boot 3.2 + Jakarta EE)**

**User Service Entity (Jakarta Persistence)**
```java
// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Invalid email format")
    private String email;
    
    @Column(nullable = false)
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number")
    private String phone;
    
    private String address;
    
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.CUSTOMER;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private boolean active = true;
}
```

**Product Service Entity with Elasticsearch Integration**
```java
// Product.java
@Entity
@Table(name = "products")
@Document(indexName = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @org.springframework.data.annotation.Id
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Field(type = FieldType.Double)
    private BigDecimal price;
    
    @Min(value = 0, message = "Stock cannot be negative")
    @Field(type = FieldType.Integer)
    private Integer stock;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private String brand;
    
    @Field(type = FieldType.Text)
    private List<String> tags;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private boolean active = true;
}
```

**Order Service Entity**
```java
// Order.java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    private String shippingAddress;
    private String billingAddress;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    private LocalDateTime deliveredAt;
}

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private String productName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
```

**Notification Service Document (MongoDB)**
```java
// Notification.java
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    @NotNull
    private String userId;
    
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @NotBlank
    private String title;
    
    @NotBlank
    private String message;
    
    @Email
    private String recipient;
    
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.PENDING;
    
    private Map<String, Object> metadata = new HashMap<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime sentAt;
    
    private String errorMessage;
    
    private Integer retryCount = 0;
}

public enum NotificationType {
    EMAIL, SMS, PUSH, IN_APP
}

public enum NotificationStatus {
    PENDING, SENT, FAILED, RETRY
}
```

### **Data Consistency Strategies**

#### **Saga Pattern Implementation**
```java
// OrderSaga.java - Distributed Transaction Management
@Component
public class OrderSaga {
    
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final OrderService orderService;
    
    @SagaOrchestrationStart
    public void processOrder(OrderCreatedEvent event) {
        try {
            // Step 1: Reserve inventory
            reserveInventory(event.getOrderId(), event.getItems());
            
            // Step 2: Process payment
            processPayment(event.getOrderId(), event.getTotalAmount());
            
            // Step 3: Confirm order
            confirmOrder(event.getOrderId());
            
        } catch (Exception e) {
            // Compensate on failure
            compensateOrder(event.getOrderId());
        }
    }
    
    private void compensateOrder(Long orderId) {
        // Rollback inventory
        releaseInventory(orderId);
        
        // Refund payment
        refundPayment(orderId);
        
        // Cancel order
        cancelOrder(orderId);
    }
}
```

---

## 📊 **Logging & Monitoring**

### **Application Logging Configuration**

#### **Logback Configuration**
```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/var/log/app.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
                <fileNamePattern>/var/log/app.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%logger{36}] - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

#### **Structured Logging Example**
```java
// Structured logging in services
@Service
public class OrderService {
    
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for user: {}, items: {}", 
            request.getUserId(), request.getItems().size());
        
        try {
            Order order = processOrder(request);
            
            log.info("Order created successfully: orderId={}, userId={}, totalAmount={}", 
                order.getId(), order.getUserId(), order.getTotalAmount());
            
            return order;
            
        } catch (Exception e) {
            log.error("Failed to create order for user: {}, error: {}", 
                request.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
}
```

### **Viewing Logs**

#### **Docker Logs Commands**
```bash
# View all service logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f user-service
docker-compose logs -f api-gateway
docker-compose logs -f order-service

# View logs with timestamps
docker-compose logs -f -t user-service

# View last 100 lines
docker-compose logs --tail=100 -f user-service

# View logs from specific time
docker-compose logs --since="2024-01-01T00:00:00" user-service
```

#### **Real-time Log Monitoring**
```bash
# Monitor all services simultaneously
docker-compose logs -f | grep "ERROR\|WARN\|Exception"

# Monitor specific patterns
docker-compose logs -f | grep "order.created"
docker-compose logs -f | grep "payment.processed"

# Monitor API Gateway routing
docker-compose logs -f api-gateway | grep "Mapped"
```

### **Application Metrics**

#### **Actuator Configuration**
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

#### **Custom Metrics**
```java
// Custom metrics in services
@Component
public class OrderMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTimer;
    
    public OrderMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.orderCreatedCounter = Counter.builder("orders.created")
            .description("Number of orders created")
            .register(meterRegistry);
        this.orderProcessingTimer = Timer.builder("order.processing.time")
            .description("Order processing time")
            .register(meterRegistry);
    }
    
    public void recordOrderCreated() {
        orderCreatedCounter.increment();
    }
    
    public void recordOrderProcessingTime(Duration duration) {
        orderProcessingTimer.record(duration);
    }
}
```

---

## 🛡️ **Error Handling & Resilience**

### **Global Exception Handler**
```java
// GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e) {
        log.error("Entity not found: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "ENTITY_NOT_FOUND",
            e.getMessage(),
            HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e) {
        log.error("Validation error: {}", e.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred",
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### **Circuit Breaker Pattern**
```java
// CircuitBreaker configuration
@Component
public class ExternalServiceClient {
    
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    
    public ExternalServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
        this.circuitBreaker = CircuitBreaker.ofDefaults("external-service");
    }
    
    public String callExternalService(String data) {
        return circuitBreaker.executeSupplier(() -> {
            return webClient.post()
                .uri("/api/external")
                .bodyValue(data)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .block();
        });
    }
}
```

### **Retry Mechanism**
```java
// Retry configuration
@Component
public class RetryableService {
    
    @Retryable(
        value = {Exception.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String processWithRetry(String data) {
        // Service call that might fail
        return externalService.process(data);
    }
    
    @Recover
    public String recover(Exception e, String data) {
        log.error("All retry attempts failed for data: {}", data, e);
        return "FAILED";
    }
}
```

---

## 🔐 **Security & Authentication**

### **JWT Token Flow**
```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant G as API Gateway
    participant A as Auth Service
    participant S as Service
    
    U->>F: Login credentials
    F->>G: POST /api/users/login
    G->>A: Forward login request
    A->>A: Validate credentials
    A-->>G: JWT token
    G-->>F: JWT token
    F->>F: Store token in localStorage
    
    F->>G: API request with token
    G->>G: Extract token from header
    G->>A: Validate token
    A-->>G: Token valid
    G->>S: Forward request
    S-->>G: Response
    G-->>F: Response
```

### **JWT Implementation**
```java
// JwtUtils.java
@Component
public class JwtUtils {
    
    private static final String SECRET = "mySecretKey";
    private static final int EXPIRATION_TIME = 86400000; // 24 hours
    
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);
        
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, SECRET)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(SECRET)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### **Security Filter**
```java
// JwtAuthenticationFilter.java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && jwtUtils.validateToken(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

---

## 🔧 **Troubleshooting Guide**

### **Common Issues & Solutions (README Aligned)**

#### **1. Service Communication Issues**
```bash
# Check service connectivity within Docker network
docker exec -it api-gateway curl http://user-service:8080/actuator/health

# Check Docker network (microservices-network)
docker network ls
docker network inspect study-microservice_microservices-network

# Check service logs for errors
docker-compose logs -f user-service | grep "ERROR\|Exception"
docker-compose logs -f api-gateway | grep "ERROR\|Exception"
```

#### **2. Database Connection Problems**
```bash
# Test PostgreSQL connection (README credentials)
docker exec -it postgres psql -U admin -d microservices_db -c "SELECT 1;"

# Test MongoDB connection
docker exec -it mongodb mongo -u admin -p password --authenticationDatabase admin

# Check database logs
docker-compose logs -f postgres
docker-compose logs -f mongodb

# Check Hibernate connection pool
docker-compose logs -f user-service | grep "HikariPool\|JPA"
```

#### **3. Message Queue Issues (RabbitMQ)**
```bash
# Access RabbitMQ Management UI (README specified)
# URL: http://localhost:15672 
# Credentials: admin/password

# Check queue status via command line
docker exec -it rabbitmq rabbitmqctl list_queues

# Check exchanges and bindings
docker exec -it rabbitmq rabbitmqctl list_exchanges
docker exec -it rabbitmq rabbitmqctl list_bindings

# Monitor message flow
docker-compose logs -f order-service | grep "Published\|OrderCreated"
docker-compose logs -f payment-service | grep "Received\|Processing"
docker-compose logs -f notification-service | grep "Received\|Sending"
```

#### **4. API Gateway Routing Problems**
```bash
# Check gateway routes configuration
curl http://localhost:8080/actuator/gateway/routes | jq

# Check gateway logs for routing
docker-compose logs -f api-gateway | grep "Mapped\|RoutePredicateFactory"

# Test service endpoints directly (README ports)
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service  
curl http://localhost:8083/actuator/health  # Order Service
curl http://localhost:8084/actuator/health  # Payment Service
curl http://localhost:8085/actuator/health  # Notification Service

# Test API Gateway routing
curl http://localhost:8080/api/users/health
curl http://localhost:8080/api/products/health
```

#### **5. Port Conflicts (README Specified Ports)**
```bash
# Check what's using the defined ports
netstat -tulpn | grep "3000\|8080\|8081\|8082\|8083\|8084\|8085"
lsof -i :3000   # Frontend
lsof -i :8080   # API Gateway
lsof -i :8081   # User Service
lsof -i :5432   # PostgreSQL
lsof -i :27017  # MongoDB
lsof -i :6379   # Redis
lsof -i :5672   # RabbitMQ
lsof -i :15672  # RabbitMQ Management
```

#### **6. Memory Issues**
```bash
# Monitor Docker resource usage
docker stats --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"

# Check individual service memory
docker stats user-service product-service order-service

# Increase Docker memory limit in Docker Desktop if needed
# Settings > Resources > Memory: Increase to 4GB+
```

### **Quick Start Commands (README Aligned)**
```bash
# Complete startup sequence
docker-compose up -d

# View all service logs
docker-compose logs -f

# Stop all services
docker-compose down

# Rebuild and restart (for code changes)
docker-compose down && docker-compose up --build -d

# Clean restart (remove volumes)
docker-compose down -v && docker-compose up --build -d
```

### **Health Check Endpoints (README Ports)**
```bash
# Frontend application
curl http://localhost:3000

# API Gateway (main entry point)
curl http://localhost:8080/actuator/health

# Individual microservices
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Order Service
curl http://localhost:8084/actuator/health  # Payment Service
curl http://localhost:8085/actuator/health  # Notification Service

# Database services
curl http://localhost:5432                   # PostgreSQL (connection test)
curl http://localhost:27017                  # MongoDB (connection test)
curl http://localhost:6379                   # Redis (connection test)
curl http://localhost:15672                  # RabbitMQ Management UI
```

### **Performance Monitoring**
```bash
# Check service metrics (Spring Boot Actuator)
curl http://localhost:8081/actuator/metrics | jq
curl http://localhost:8081/actuator/metrics/http.server.requests | jq

# Check JVM metrics
curl http://localhost:8081/actuator/metrics/jvm.memory.used | jq
curl http://localhost:8081/actuator/metrics/jvm.threads.live | jq

# Database performance monitoring
docker exec -it postgres psql -U admin -d microservices_db -c "
SELECT pid, usename, application_name, state, query_start, query 
FROM pg_stat_activity 
WHERE state = 'active';"

# Redis performance monitoring
docker exec -it redis redis-cli info stats
docker exec -it redis redis-cli monitor
```

### **Log Analysis Commands**
```bash
# Search for specific patterns across all services
docker-compose logs 2>&1 | grep -i "error\|exception\|failed"

# Monitor API calls
docker-compose logs -f api-gateway | grep -E "POST|GET|PUT|DELETE"

# Monitor database queries (if SQL logging enabled)
docker-compose logs -f user-service | grep -i "select\|insert\|update\|delete"

# Monitor message processing
docker-compose logs -f | grep -i "rabbitmq\|message\|event"
```

### **Development Workflow Commands**
```bash
# Restart single service after code changes
docker-compose restart user-service
docker-compose logs -f user-service

# Rebuild single service
docker-compose build user-service
docker-compose up -d user-service

# Access service containers for debugging
docker exec -it user-service bash
docker exec -it postgres psql -U admin -d microservices_db
docker exec -it mongodb mongo -u admin -p password
docker exec -it redis redis-cli
```

# Monitor resource usage
docker stats

# Check database performance
docker exec -it postgres psql -U admin -d microservices_db -c "SELECT * FROM pg_stat_activity;"
```

---

## 📈 **Monitoring & Observability**

### **Distributed Tracing Setup**
```yaml
# application.yml - Add to all services
spring:
  sleuth:
    enabled: true
    sampler:
      probability: 1.0
  zipkin:
    base-url: http://zipkin:9411
```

### **Metrics Collection**
```java
// Custom metrics for business logic
@Component
public class BusinessMetrics {
    
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        Counter.builder("business.orders.created")
            .tag("user", event.getUserId().toString())
            .register(meterRegistry)
            .increment();
    }
    
    @EventListener
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        Timer.builder("business.payment.processing.time")
            .tag("status", event.getStatus().toString())
            .register(meterRegistry)
            .record(event.getProcessingTime(), TimeUnit.MILLISECONDS);
    }
}
```

---

## 📊 **Architecture Summary & Key Decisions**

### **Technology Stack (README Aligned)**
| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Backend Framework** | Spring Boot | 3.2+ | Modern Java framework with Jakarta EE 9+ |
| **Java Version** | OpenJDK | 17+ | Required for Spring Boot 3.2 |
| **Build Tool** | Gradle | 8.5+ | Dependency management and builds |
| **API Gateway** | Spring Cloud Gateway | Latest | Request routing and cross-cutting concerns |
| **Frontend** | Vue.js | 3.x | Modern reactive SPA framework |
| **State Management** | Pinia | Latest | Vue.js state management |
| **Primary Database** | PostgreSQL | 13+ | ACID compliance and structured data |
| **Document Database** | MongoDB | 5.0+ | Flexible schema for notifications |
| **Cache** | Redis | 6.2+ | High-performance caching and sessions |
| **Message Queue** | RabbitMQ | 3.x | Asynchronous event-driven communication |
| **Search Engine** | Elasticsearch | 7.x+ | Full-text search capabilities |
| **Containerization** | Docker | 20.x+ | Local development environment |
| **Orchestration** | Docker Compose | 3.8 | Multi-container application management |

### **Port Allocation (README Specified)**
```
Frontend (Vue.js)     → localhost:3000
API Gateway           → localhost:8080
User Service         → localhost:8081
Product Service      → localhost:8082
Order Service        → localhost:8083
Payment Service      → localhost:8084
Notification Service → localhost:8085
PostgreSQL           → localhost:5432
MongoDB              → localhost:27017
Redis                → localhost:6379
RabbitMQ             → localhost:5672
RabbitMQ Management  → localhost:15672
```

### **Database Strategy (README Aligned)**
- **User Service**: PostgreSQL (structured user data, ACID transactions)
- **Product Service**: PostgreSQL + Elasticsearch (search optimization) + Redis (caching)
- **Order Service**: PostgreSQL + Redis (fast lookups and caching)
- **Payment Service**: PostgreSQL (transaction integrity and financial data)
- **Notification Service**: MongoDB (flexible message formats and metadata)

### **Communication Patterns**
1. **Frontend ↔ API Gateway**: HTTP/REST via Vue.js Axios client
2. **API Gateway ↔ Services**: HTTP/REST with circuit breakers and load balancing
3. **Service ↔ Service**: WebClient for synchronous HTTP calls
4. **Service ↔ Message Queue**: RabbitMQ for asynchronous event-driven communication
5. **Service ↔ Database**: JPA/Hibernate for PostgreSQL, Spring Data MongoDB

### **Key Architectural Benefits**
- **Local Development**: Complete environment runs on single machine
- **Service Independence**: Each service can be developed, tested, and deployed separately
- **Technology Diversity**: Different databases for different service needs
- **Event-Driven**: Asynchronous processing with RabbitMQ for better scalability
- **Caching Strategy**: Redis for improved performance on frequently accessed data
- **Search Capability**: Elasticsearch for advanced product search features
- **Modern Stack**: Spring Boot 3.2 + Java 17 + Jakarta EE for future compatibility

### **Learning Path Outcomes**
By implementing this architecture, you will master:
- **Modern Spring Boot 3.x**: Jakarta EE migration, enhanced security, performance improvements
- **Microservices Design**: Service decomposition, communication patterns, data management
- **Vue.js Frontend Integration**: SPA development with modern reactive patterns
- **Docker Orchestration**: Multi-container application development and deployment
- **Event-Driven Architecture**: Asynchronous messaging and eventual consistency
- **Database Management**: Multi-database strategies and data consistency patterns
- **Monitoring & Observability**: Logging, metrics, and distributed system debugging

This comprehensive guide demonstrates exactly how modern microservices communicate, handle data, manage failures, and can be monitored in production environments while providing a solid foundation for learning distributed systems architecture.