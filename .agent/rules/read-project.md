---
trigger: always_on
glob:
description: Understand and navigate the microservices project structure
---

# Read Project Skill

Use this skill to understand and navigate this microservices e-commerce project.

## Quick Project Overview

**Project Type**: Microservices E-Commerce Platform  
**Backend**: Java 17+ / Spring Boot 3.2+ (Jakarta EE 9+)  
**Frontend**: Vue.js 3 + Pinia + Vuetify  
**Build Tool**: Gradle 8.5+

## Project Structure

```
study-microservice/
├── 📄 README.md              # Architecture overview
├── 📄 SERVICES_FLOW.md       # Communication patterns  
├── 📄 START_GUIDE.md         # Quick start guide
├── 📄 ENHANCEMENTS_TODO.md   # Enhancement roadmap (13 tasks)
│
├── 🔧 config-server/         # Spring Cloud Config Server
├── 🔍 discovery-service/     # Eureka Service Discovery
├── 🌐 api-gateway/           # Spring Cloud Gateway
│
├── 👤 user-service/          # User auth & profiles (PostgreSQL)
├── 📦 product-service/       # Product catalog (PostgreSQL + Redis)
├── 🛒 order-service/         # Order management (PostgreSQL + RabbitMQ)
├── 💳 payment-service/       # Payment processing (PostgreSQL)
├── 📧 notification-service/  # Notifications (MongoDB + RabbitMQ)
│
├── 🖥️ frontend/              # Vue.js SPA (planned)
├── 📁 config-repo/           # External config files
└── 📁 z-init-db/             # Database initialization scripts
```

## Step-by-Step: Understanding the Project

### Step 1: Read Core Documentation
```bash
# Start with these files in order:
1. view_file README.md          # Architecture & tech stack
2. view_file START_GUIDE.md     # How to run the project
3. view_file SERVICES_FLOW.md   # Service communication details
4. view_file ENHANCEMENTS_TODO.md # What's implemented & planned
```

### Step 2: Explore a Service

Each Spring Boot service follows this structure:
```
<service-name>/
├── build.gradle                 # Dependencies & build config
├── Dockerfile                   # Container definition
├── src/main/java/com/microservices/<service>/
│   ├── <Service>Application.java    # Main class
│   ├── config/                       # Spring configs
│   ├── controller/                   # REST endpoints
│   ├── service/                      # Business logic
│   ├── repository/                   # Database access
│   ├── entity/                       # JPA entities
│   ├── dto/                          # Data transfer objects
│   └── exception/                    # Custom exceptions
├── src/main/resources/
│   ├── application.yml              # Local config
│   └── bootstrap.yml                # Config server settings
└── src/test/                        # Tests
```

### Step 3: Key Files to Check

#### For Understanding Architecture
| File | Purpose |
|------|---------|
| `docker-compose.yml` | All services & infrastructure |
| `api-gateway/src/.../config/GatewayConfig.java` | Route configurations |
| `config-repo/*.properties` | Externalized configurations |

#### For Understanding a Service
| File Pattern | Purpose |
|--------------|---------|
| `*Application.java` | Main entry point, annotations |
| `*Controller.java` | API endpoints |
| `*Service.java` | Business logic |
| `*Repository.java` | Database operations |
| `application.yml` | Service configuration |

### Step 4: Check Implementations

#### Find All Controllers
```bash
find_by_name Pattern="*Controller.java" in each service's src/main/java
```

#### Find Service-to-Service Calls
```bash
grep_search Query="WebClient" to find inter-service communication
grep_search Query="@RabbitListener" to find async message handlers
```

#### Find Database Entities
```bash
find_by_name Pattern="*.java" in <service>/src/main/java/.../entity/
```

## Infrastructure Services

### Config Server (Port 8888)
- Provides centralized configuration from `config-repo/`
- Other services fetch config on startup via `bootstrap.yml`

### Discovery Service (Port 8761)
- Eureka Server for service registration
- UI: `http://localhost:8761`
- Services register themselves as Eureka clients

### API Gateway (Port 8080)
- Single entry point for all frontend requests
- Routes to services based on path patterns
- Handles circuit breaking with Resilience4j

## Service Communication Patterns

### Synchronous (REST)
```
Frontend → API Gateway → Target Service → Response
```

### Asynchronous (RabbitMQ)
```
Order Service → RabbitMQ → Payment Service
                         → Notification Service
```

### Service Discovery Flow
```
Service starts → Registers with Eureka → Other services discover via Eureka
```

## Quick Commands

### Check Service Health
```bash
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Order Service
```

### View Eureka Dashboard
```
http://localhost:8761
```

### View RabbitMQ Management
```
http://localhost:15672 (admin/password)
```

## Tech Stack Reference

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 17+ | Required for Spring Boot 3 |
| Spring Boot | 3.2.0 | Jakarta EE, modern security |
| Spring Security | 6.x | SecurityFilterChain config |
| Gradle | 8.5+ | Build tool |
| PostgreSQL | 13+ | Primary database |
| MongoDB | 5.0+ | Notification storage |
| Redis | 6.2+ | Caching |
| RabbitMQ | 3.x | Message queue |
| Vue.js | 3.x | Frontend framework |

## Key Spring Dependencies

- `spring-boot-starter-web` - REST APIs
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-actuator` - Health endpoints
- `spring-cloud-starter-netflix-eureka-client` - Service discovery
- `spring-cloud-starter-config` - Config server client
- `spring-cloud-starter-gateway` - API Gateway
- `resilience4j-spring-boot3` - Circuit breaker

## Enhancement Progress (Tier 1 Completed)

✅ Health Checks - Actuator with dependency indicators  
✅ Centralized Config - Spring Cloud Config Server  
✅ Service Discovery - Eureka Server  
✅ Circuit Breaker - Resilience4j integration  

See `ENHANCEMENTS_TODO.md` for Tier 2 and Tier 3 tasks.
---

