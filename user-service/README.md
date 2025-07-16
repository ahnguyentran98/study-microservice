# User Service - Microservices Architecture

This User Service handles user authentication and profile management for the microservices e-commerce platform. Authorization is handled by the API Gateway.

## 🏗️ Architecture Overview

The User Service is built with:
- **Spring Boot 3.2.0** - Main framework (Jakarta EE 9+)
- **Spring Security 6.x** - Authentication and authorization
- **JWT** - Token-based authentication
- **PostgreSQL** - Primary database
- **JPA/Hibernate 6.3.x** - Database ORM
- **Java 17** - Runtime environment
- **Gradle 8.5** - Build tool

## 📂 Project Structure

```
user-service/
├── src/
│   ├── main/
│   │   ├── java/com/microservices/userservice/
│   │   │   ├── config/
│   │   │   │   ├── AuthEntryPointJwt.java      # JWT authentication entry point
│   │   │   │   ├── PasswordEncoderConfig.java  # Password encryption config
│   │   │   │   └── WebSecurityConfig.java      # Spring Security configuration
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java         # Login/Register endpoints
│   │   │   │   └── UserController.java         # User profile endpoints
│   │   │   ├── dto/
│   │   │   │   ├── JwtResponse.java            # JWT response model
│   │   │   │   ├── LoginRequest.java           # Login request model
│   │   │   │   └── UserRegistrationRequest.java # Registration request model
│   │   │   ├── model/
│   │   │   │   └── User.java                   # User entity
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java         # Data access layer
│   │   │   ├── security/
│   │   │   │   ├── AuthTokenFilter.java        # JWT filter
│   │   │   │   ├── JwtUtils.java              # JWT utility functions
│   │   │   │   └── UserPrincipal.java         # User principal for security
│   │   │   ├── service/
│   │   │   │   └── UserService.java           # Business logic
│   │   │   └── UserServiceApplication.java    # Main application class
│   │   └── resources/
│   │       └── application.properties         # Configuration
├── build.gradle                               # Build configuration
├── Dockerfile                                 # Container configuration
└── README.md                                  # This file
```

## 🚀 Build Steps

### 1. **Project Setup**
```bash
# Created directory structure
mkdir -p user-service/src/main/java/com/microservices/userservice
mkdir -p user-service/src/main/resources
mkdir -p user-service/src/test/java/com/microservices/userservice
```

### 2. **Gradle Configuration**
Created `build.gradle` with dependencies:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Spring Boot Starter Actuator
- JWT (jjwt) libraries
- PostgreSQL driver
- Lombok for boilerplate reduction

### 3. **Database Configuration**
Configured `application.properties`:
```properties
# Server Configuration
server.port=8080
spring.application.name=user-service

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/microservices_db
spring.datasource.username=admin
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

### 4. **Entity Layer**
Created `User.java` entity with:
- JPA annotations (`@Entity`, `@Table`, `@Id`, etc.)
- Validation annotations (`@Email`, `@NotBlank`, `@Size`)
- Lombok annotations (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`)
- User roles (USER, ADMIN)
- Timestamps (created_at, updated_at)

### 5. **Repository Layer**
Created `UserRepository.java` interface extending `JpaRepository`:
- `findByEmail(String email)` - Find user by email
- `existsByEmail(String email)` - Check if email exists


### 6. **Security Layer**
Implemented JWT-based authentication:

**JwtUtils.java** - JWT token management:
- Token generation
- Token validation
- Extract email from token

**UserPrincipal.java** - Spring Security user details:
- Implements `UserDetails` interface
- Maps User entity to security context

**AuthTokenFilter.java** - JWT authentication filter:
- Intercepts requests
- Validates JWT tokens
- Sets security context

**PasswordEncoderConfig.java** - Password encryption:
- BCrypt password encoder bean

**WebSecurityConfig.java** - Spring Security configuration:
- JWT authentication setup
- Endpoint security rules
- CORS configuration

### 7. **Service Layer**
Created `UserService.java` with:
- User registration logic
- Password encryption
- User details service implementation
- Profile management operations

### 8. **Controller Layer**
**AuthController.java** - Authentication endpoints:
- `POST /api/users/register` - User registration
- `POST /api/users/login` - User login with JWT response

**UserController.java** - Profile management:
- `GET /api/users/profile` - Get current user profile
- `PUT /api/users/profile` - Update user profile
- `GET /api/users/{id}` - Get user by ID (admin only)
- `DELETE /api/users/{id}` - Delete user (admin only)

### 9. **DTO Layer**
Created data transfer objects:
- `UserRegistrationRequest.java` - Registration payload
- `LoginRequest.java` - Login payload
- `JwtResponse.java` - JWT authentication response

### 10. **Main Application**
Created `UserServiceApplication.java` with `@SpringBootApplication`

### 11. **Docker Support**
Created `Dockerfile` for containerization:
```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY build/libs/user-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 🛠️ Prerequisites

- **Java 17+** (required for Spring Boot 3)
- **Gradle 8.5+** (compatible with Spring Boot 3)
- **PostgreSQL 13+**
- **Docker** (optional, for containerization)

## 🚀 Running the Service

### 1. **Database Setup**
```bash
# Start PostgreSQL container
docker run -d --name postgres-microservices \
  -e POSTGRES_DB=microservices_db \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:13
```

### 2. **Build the Service**
```bash
./gradlew build
```

### 3. **Run the Service**
```bash
# Option 1: Using Gradle
./gradlew bootRun

# Option 2: Using JAR
java -jar build/libs/user-service-1.0.0.jar

# Option 3: Using Docker
docker build -t user-service .
docker run -p 8080:8080 --link postgres-microservices:postgres user-service
```

## 🔄 Spring Boot 3 Migration

This service has been migrated from Spring Boot 2.7 to Spring Boot 3.2. Key changes include:

### **Breaking Changes Addressed:**
- **Namespace Migration**: `javax.*` → `jakarta.*` (JPA, Validation, Servlet)
- **Spring Security**: Replaced `WebSecurityConfigurerAdapter` with `SecurityFilterChain`
- **Method Security**: `@EnableGlobalMethodSecurity` → `@EnableMethodSecurity`
- **Java Version**: Minimum requirement upgraded to Java 17
- **Gradle**: Updated to version 8.5 for compatibility

### **Updated Dependencies:**
- Spring Boot: `2.7.0` → `3.2.0`
- Spring Security: `5.x` → `6.x`
- Hibernate: `5.6.x` → `6.3.x`
- Tomcat: `9.x` → `10.x`
- Jakarta EE: `8` → `9+`

### **Configuration Updates:**
- Modern Spring Security with lambda DSL
- Updated `requestMatchers()` instead of `antMatchers()`
- Enhanced authentication provider configuration
- Improved CORS and CSRF handling

## 📡 API Endpoints

### Authentication Endpoints

#### Register User
```bash
POST /api/users/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "address": "123 Main St, City, Country"
}
```

#### Login User
```bash
POST /api/users/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "USER"
}
```

### Profile Management Endpoints

#### Get User Profile
```bash
GET /api/users/profile
Authorization: Bearer <JWT_TOKEN>
```

#### Update User Profile
```bash
PUT /api/users/profile
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "firstName": "John Updated",
  "lastName": "Doe Updated",
  "phone": "+9876543210",
  "address": "456 New St, New City, Country"
}
```

#### Get User by ID (Admin Only)
```bash
GET /api/users/{id}
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

#### Delete User (Admin Only)
```bash
DELETE /api/users/{id}
Authorization: Bearer <ADMIN_JWT_TOKEN>
```

### Health Check
```bash
GET /actuator/health
```


## 🔒 Security Features

- **Authentication Only**: The User Service is responsible for authenticating users and issuing JWT tokens. Authorization is enforced by the API Gateway.
- **JWT Authentication** - Stateless token-based auth
- **Password Encryption** - BCrypt hashing
- **Input Validation** - Email format, password strength
- **CORS Configuration** - Cross-origin resource sharing
- **Security Headers** - Protection against common attacks

## 🗄️ Database Schema

The service automatically creates the following table:

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    phone VARCHAR(255),
    address VARCHAR(255),
    role VARCHAR(255) DEFAULT 'USER',
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

## 🧪 Testing

Test the service endpoints using curl or Postman:

1. **Register a new user**
2. **Login to get JWT token**
3. **Use token to access protected endpoints**
4. **Test profile management operations**

## 🐳 Docker Integration

Build and run with Docker:
```bash
# Build image
docker build -t user-service .

# Run container
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/microservices_db \
  user-service
```

## 🔧 Configuration

Key configuration properties in `application.properties`:

- `server.port` - Service port (default: 8080)
- `spring.datasource.*` - Database connection settings
- `jwt.secret` - JWT signing secret
- `jwt.expiration` - Token expiration time (24 hours)
- `management.endpoints.*` - Actuator endpoints

## 🚨 Troubleshooting

### Common Issues:

1. **Database Connection Failed**
   - Ensure PostgreSQL is running
   - Check connection credentials
   - Verify database exists

2. **JWT Token Invalid**
   - Check token expiration
   - Verify JWT secret configuration
   - Ensure Bearer prefix in Authorization header

3. **Circular Dependency Error**
   - Resolved by separating PasswordEncoder configuration
   - Ensure proper dependency injection

4. **Port Already in Use**
   - Change port in `application.properties`
   - Stop conflicting services

## 📚 Next Steps

This User Service is designed to integrate with:
- **API Gateway** - Central routing and load balancing
- **Product Service** - Product catalog management
- **Order Service** - Order processing and management
- **Payment Service** - Payment processing
- **Notification Service** - Email and SMS notifications

The service follows microservices best practices and is ready for production deployment with proper environment configuration.