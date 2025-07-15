# 🔄 Microservices Communication Flow & Mechanisms

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

#### **API Gateway Routing Configuration**
```java
// GatewayConfig.java
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
```

#### **How Request Routing Works**
1. **Frontend** sends HTTP request to `http://localhost:3000/api/users/profile`
2. **Nginx** (in frontend container) proxies to `http://api-gateway:8080/api/users/profile`
3. **API Gateway** matches `/api/users/**` pattern
4. **Routes** request to `http://user-service:8080/api/users/profile`
5. **User Service** processes request and returns response
6. **Response** flows back through the same path

### **2. Service-to-Service Communication**

#### **WebClient Configuration**
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
}
```

#### **Service Call Example**
```java
// OrderService.java
@Service
public class OrderService {
    
    private final WebClient userServiceClient;
    private final WebClient productServiceClient;
    
    public Order createOrder(OrderRequest request) {
        // 1. Validate user exists
        User user = userServiceClient
            .get()
            .uri("/api/users/{id}", request.getUserId())
            .retrieve()
            .bodyToMono(User.class)
            .block();
            
        // 2. Validate products and check inventory
        List<Product> products = new ArrayList<>();
        for (OrderItemRequest item : request.getItems()) {
            Product product = productServiceClient
                .get()
                .uri("/api/products/{id}", item.getProductId())
                .retrieve()
                .bodyToMono(Product.class)
                .block();
                
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException("Not enough stock for " + product.getName());
            }
            products.add(product);
        }
        
        // 3. Create order
        Order order = new Order();
        order.setUserId(user.getId());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(calculateTotal(request.getItems(), products));
        order.setCreatedAt(LocalDateTime.now());
        
        // 4. Save order
        Order savedOrder = orderRepository.save(order);
        
        // 5. Update inventory
        updateInventory(request.getItems());
        
        // 6. Publish order created event
        orderEventPublisher.publishOrderCreated(savedOrder);
        
        return savedOrder;
    }
}
```

---

## 🔄 **Request Flow Examples**

### **Example 1: User Registration Flow**

```mermaid
sequenceDiagram
    participant F as Frontend
    participant G as API Gateway
    participant U as User Service
    participant DB as PostgreSQL
    
    F->>G: POST /api/users/register
    G->>U: Forward request
    U->>U: Validate user data
    U->>U: Hash password
    U->>DB: Save user
    DB-->>U: User saved
    U->>U: Generate JWT token
    U-->>G: Return user + token
    G-->>F: Return response
    F->>F: Store token & redirect
```

### **Example 2: Product Search Flow**

```mermaid
sequenceDiagram
    participant F as Frontend
    participant G as API Gateway
    participant P as Product Service
    participant R as Redis
    participant DB as PostgreSQL
    
    F->>G: POST /api/products/search
    G->>P: Forward search request
    P->>R: Check cache
    alt Cache Hit
        R-->>P: Return cached results
    else Cache Miss
        P->>DB: Search products
        DB-->>P: Return results
        P->>R: Cache results
    end
    P-->>G: Return products
    G-->>F: Return response
    F->>F: Display products
```

### **Example 3: Order Creation Flow**

```mermaid
sequenceDiagram
    participant F as Frontend
    participant G as API Gateway
    participant O as Order Service
    participant U as User Service
    participant P as Product Service
    participant Pay as Payment Service
    participant N as Notification Service
    participant MQ as RabbitMQ
    participant DB as PostgreSQL
    
    F->>G: POST /api/orders
    G->>O: Forward order request
    O->>U: Validate user
    U-->>O: User valid
    O->>P: Check product inventory
    P-->>O: Inventory available
    O->>DB: Save order
    DB-->>O: Order saved
    O->>MQ: Publish OrderCreated event
    MQ->>Pay: Process payment
    Pay->>Pay: Process payment
    Pay->>MQ: Publish PaymentProcessed event
    MQ->>N: Send order confirmation
    MQ->>O: Update order status
    O-->>G: Return order
    G-->>F: Return response
```

---

## 🎯 **Service Discovery & Routing**

### **Docker Network Communication**

#### **Docker Compose Network Configuration**
```yaml
# docker-compose.yml
networks:
  microservices-network:
    driver: bridge

services:
  user-service:
    networks:
      - microservices-network
    # Service accessible as 'user-service' hostname
    
  product-service:
    networks:
      - microservices-network
    # Service accessible as 'product-service' hostname
```

#### **Service Resolution Process**
1. **DNS Resolution**: Docker's embedded DNS server resolves service names
2. **Network Discovery**: Services find each other by container names
3. **Load Balancing**: Docker handles load balancing between service instances
4. **Health Checks**: Services can check each other's health

### **API Gateway Service Discovery**
```java
// application.yml in API Gateway
spring:
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
                fallbackUri: forward:/fallback
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

### **Database per Service Pattern**

#### **Service-Specific Database Configuration**
```yaml
# User Service - application.yml
spring:
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
```

```yaml
# Notification Service - application.yml
spring:
  data:
    mongodb:
      uri: mongodb://admin:password@mongodb:27017/notifications
      database: notifications
```

#### **Database Schema Management**
```java
// User Service Entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

```java
// Notification Service Document
@Document(collection = "notifications")
public class Notification {
    @Id
    private String id;
    
    private String userId;
    private NotificationType type;
    private String title;
    private String message;
    private String recipient;
    private NotificationStatus status;
    private Map<String, Object> metadata;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
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

### **Common Issues & Solutions**

#### **1. Service Communication Issues**
```bash
# Check service connectivity
docker exec -it api-gateway curl http://user-service:8080/actuator/health

# Check network connectivity
docker network inspect study-microservice_microservices-network

# Check service logs
docker-compose logs -f user-service | grep "ERROR"
```

#### **2. Database Connection Problems**
```bash
# Check database connectivity
docker exec -it postgres psql -U admin -d microservices_db -c "SELECT 1;"

# Check database logs
docker-compose logs -f postgres

# Check connection pool
docker-compose logs -f user-service | grep "HikariPool"
```

#### **3. Message Queue Issues**
```bash
# Check RabbitMQ management UI
# Access: http://localhost:15672 (admin/password)

# Check queue status
docker exec -it rabbitmq rabbitmqctl list_queues

# Check message flow
docker-compose logs -f order-service | grep "Published"
docker-compose logs -f payment-service | grep "Received"
```

#### **4. API Gateway Routing Problems**
```bash
# Check gateway routes
curl http://localhost:8080/actuator/gateway/routes

# Check gateway logs
docker-compose logs -f api-gateway | grep "Mapped"

# Test service endpoints directly
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
```

### **Health Check Endpoints**
```bash
# Check all service health
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Order Service
curl http://localhost:8084/actuator/health  # Payment Service
curl http://localhost:8085/actuator/health  # Notification Service
```

### **Performance Monitoring**
```bash
# Check service metrics
curl http://localhost:8081/actuator/metrics
curl http://localhost:8081/actuator/metrics/http.server.requests

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

This comprehensive guide shows exactly how the microservices communicate, handle data, manage failures, and can be monitored in production. The system demonstrates real-world patterns and practices used in modern distributed systems.