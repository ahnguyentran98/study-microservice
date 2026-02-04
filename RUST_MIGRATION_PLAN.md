# 🦀 Rust Migration Plan
## Migrating Microservices from Java/Spring Boot to Rust

> **Goal:** Replace Java/Spring Boot backend services with Rust equivalents while maintaining the same architecture and functionality.

---

## 📊 Current State vs Target State

| Component | Current (Java) | Target (Rust) |
|-----------|----------------|---------------|
| **Web Framework** | Spring Boot | Axum / Actix-web |
| **Database ORM** | JPA/Hibernate | Diesel / SQLx / SeaORM |
| **Async Runtime** | Virtual Threads | Tokio |
| **HTTP Client** | WebClient (Reactor) | Reqwest / Hyper |
| **Message Queue** | Spring AMQP | Lapin (RabbitMQ) |
| **JSON Serialization** | Jackson | Serde |
| **Validation** | Bean Validation | Validator |
| **Configuration** | application.yml | config-rs / TOML |
| **Logging** | Logback/SLF4J | tracing / log |
| **Security/JWT** | Spring Security | jsonwebtoken |
| **Dependency Injection** | Spring IoC | Manual / axum State |

---

## 🎯 Why Migrate to Rust?

### Benefits:
✅ **Performance**: 2-5x faster than JVM in most scenarios  
✅ **Memory Safety**: No null pointer exceptions, no data races  
✅ **Lower Resource Usage**: Smaller memory footprint (no GC overhead)  
✅ **Modern Concurrency**: Async/await with Tokio is excellent  
✅ **Zero-cost Abstractions**: High-level code, low-level performance  
✅ **Learning Experience**: Master systems programming concepts  

### Challenges:
⚠️ **Steeper Learning Curve**: Ownership, lifetimes, borrowing  
⚠️ **Longer Compilation**: Rust compiles slower than Java  
⚠️ **Less Mature Ecosystem**: Fewer batteries-included frameworks  
⚠️ **Smaller Community**: Spring Boot has much larger ecosystem  
⚠️ **More Boilerplate**: No reflection, more explicit code  

---

## 🔧 Rust Ecosystem Mapping

### Core Frameworks Comparison

#### Option A: **Axum** (Recommended)
```toml
[dependencies]
axum = "0.7"
tokio = { version = "1", features = ["full"] }
tower = "0.4"
```

**Pros:**
- Modern, ergonomic API
- Built on Tokio/Tower (production-proven)
- Type-safe routing
- Excellent middleware support
- Great documentation

**Cons:**
- Newer (less battle-tested than Actix)
- Smaller ecosystem of plugins

#### Option B: **Actix-web**
```toml
[dependencies]
actix-web = "4"
actix-rt = "2"
```

**Pros:**
- Very mature and stable
- Excellent performance benchmarks
- Large ecosystem of plugins
- Proven in production

**Cons:**
- More complex API
- Actor model can be overkill

### Database Libraries

#### Option A: **SQLx** (Recommended)
```toml
[dependencies]
sqlx = { version = "0.7", features = ["runtime-tokio", "postgres", "macros"] }
```

**Pros:**
- Compile-time checked SQL queries
- Async-first
- No ORM overhead
- Works with raw SQL

**Cons:**
- Less abstraction than ORM
- Requires database connection at compile time

#### Option B: **Diesel**
```toml
[dependencies]
diesel = { version = "2.1", features = ["postgres"] }
```

**Pros:**
- Type-safe query builder
- Strong type system
- Good migration tools

**Cons:**
- Synchronous (blocking)
- Requires proc macros

#### Option C: **SeaORM**
```toml
[dependencies]
sea-orm = { version = "0.12", features = ["sqlx-postgres", "runtime-tokio"] }
```

**Pros:**
- Most similar to Hibernate/JPA
- Async support
- Good documentation

**Cons:**
- Newer, less mature
- Can be verbose

---

## 📋 Service-by-Service Migration Plan

### Phase 1: Infrastructure Setup (Week 1)
- [ ] Set up Rust development environment
- [ ] Create workspace structure for all services
- [ ] Set up Docker build for Rust services
- [ ] Create shared library crates for common code

### Phase 2: Simplest Service First (Week 2-3)
**Start with: Notification Service**

**Why start here:**
- Uses MongoDB (simpler than relational DB)
- Only consumes events (no complex service calls)
- Limited business logic
- Good for learning Rust basics

**Stack:**
```toml
[dependencies]
axum = "0.7"
tokio = "1"
mongodb = "2"
lapin = "2"  # RabbitMQ
serde = { version = "1", features = ["derive"] }
serde_json = "1"
tracing = "0.1"
tracing-subscriber = "0.3"
```

### Phase 3: Database-Heavy Service (Week 4-5)
**Migrate: User Service**

**Why second:**
- Relatively simple CRUD operations
- Good for learning PostgreSQL with Rust
- Practice with authentication/JWT
- No service-to-service calls

**Additional dependencies:**
```toml
sqlx = { version = "0.7", features = ["postgres", "runtime-tokio", "macros"] }
argon2 = "0.5"  # Password hashing
jsonwebtoken = "9"
validator = { version = "0.16", features = ["derive"] }
```

### Phase 4: Service Communication (Week 6-7)
**Migrate: Product Service**

**Why third:**
- Introduces Redis caching
- More complex queries
- Search functionality
- Good for learning caching patterns

**Additional dependencies:**
```toml
redis = { version = "0.23", features = ["tokio-comp", "connection-manager"] }
deadpool-redis = "0.13"  # Connection pooling
```

### Phase 5: Complex Orchestration (Week 8-9)
**Migrate: Order Service**

**Why fourth:**
- Makes HTTP calls to other services
- Event publishing to RabbitMQ
- Transaction management
- Complex business logic

**Additional dependencies:**
```toml
reqwest = { version = "0.11", features = ["json"] }
tower = { version = "0.4", features = ["timeout", "retry"] }
```

### Phase 6: External Integration (Week 10)
**Migrate: Payment Service**

**Why fifth:**
- External API integration (Stripe)
- Error handling for external failures
- Idempotency patterns

### Phase 7: Gateway (Week 11)
**Migrate: API Gateway**

**Stack options:**
- **Axum** with middleware for routing
- **Pingora** (Cloudflare's proxy - advanced)
- Keep existing Spring Cloud Gateway (hybrid approach)

---

## 🏗️ Recommended Project Structure

```
study-microservice-rust/
├── Cargo.toml                    # Workspace root
├── docker-compose.yml            # Updated for Rust services
│
├── shared/                       # Shared libraries
│   ├── common/
│   │   ├── src/
│   │   │   ├── error.rs         # Common error types
│   │   │   ├── jwt.rs           # JWT utilities
│   │   │   └── lib.rs
│   │   └── Cargo.toml
│   │
│   └── messaging/
│       ├── src/
│       │   ├── rabbitmq.rs      # RabbitMQ client
│       │   └── lib.rs
│       └── Cargo.toml
│
├── services/
│   ├── user-service/
│   │   ├── src/
│   │   │   ├── main.rs
│   │   │   ├── config.rs
│   │   │   ├── handlers/
│   │   │   │   ├── mod.rs
│   │   │   │   ├── auth.rs
│   │   │   │   └── users.rs
│   │   │   ├── models/
│   │   │   │   ├── mod.rs
│   │   │   │   └── user.rs
│   │   │   ├── repository/
│   │   │   │   ├── mod.rs
│   │   │   │   └── user_repository.rs
│   │   │   └── services/
│   │   │       ├── mod.rs
│   │   │       └── user_service.rs
│   │   ├── Cargo.toml
│   │   ├── Dockerfile
│   │   └── migrations/          # SQL migrations
│   │
│   ├── product-service/
│   ├── order-service/
│   ├── payment-service/
│   ├── notification-service/
│   └── api-gateway/
│
└── frontend/                     # Keep Vue.js unchanged
```

### Workspace Cargo.toml
```toml
[workspace]
members = [
    "shared/common",
    "shared/messaging",
    "services/user-service",
    "services/product-service",
    "services/order-service",
    "services/payment-service",
    "services/notification-service",
    "services/api-gateway",
]

[workspace.package]
version = "0.1.0"
edition = "2021"
rust-version = "1.75"

[workspace.dependencies]
# Web framework
axum = "0.7"
tower = { version = "0.4", features = ["full"] }
tower-http = { version = "0.5", features = ["full"] }

# Async runtime
tokio = { version = "1", features = ["full"] }

# Database
sqlx = { version = "0.7", features = ["runtime-tokio", "postgres", "macros"] }
mongodb = "2"
redis = { version = "0.23", features = ["tokio-comp"] }

# Serialization
serde = { version = "1", features = ["derive"] }
serde_json = "1"

# Logging
tracing = "0.1"
tracing-subscriber = { version = "0.3", features = ["env-filter"] }

# Error handling
anyhow = "1"
thiserror = "1"

# HTTP client
reqwest = { version = "0.11", features = ["json"] }

# Messaging
lapin = "2"

# Utilities
uuid = { version = "1", features = ["v4", "serde"] }
chrono = { version = "0.4", features = ["serde"] }
validator = { version = "0.16", features = ["derive"] }
```

---

## 📝 Code Comparison Examples

### Example 1: User Model

**Java (JPA):**
```java
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
    
    @CreationTimestamp
    private LocalDateTime createdAt;
}
```

**Rust (SQLx):**
```rust
use sqlx::FromRow;
use serde::{Deserialize, Serialize};
use chrono::{DateTime, Utc};

#[derive(Debug, Clone, FromRow, Serialize, Deserialize)]
pub struct User {
    pub id: i64,
    pub email: String,
    #[serde(skip_serializing)]
    pub password: String,
    pub created_at: DateTime<Utc>,
}

#[derive(Debug, Deserialize, Validate)]
pub struct CreateUserRequest {
    #[validate(email)]
    pub email: String,
    
    #[validate(length(min = 8))]
    pub password: String,
}
```

### Example 2: REST Controller

**Java (Spring Boot):**
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.ok(new UserResponse(user));
    }
}
```

**Rust (Axum):**
```rust
use axum::{
    extract::State,
    http::StatusCode,
    Json,
    Router,
    routing::post,
};

pub fn routes() -> Router<AppState> {
    Router::new()
        .route("/api/users/register", post(register))
}

async fn register(
    State(state): State<AppState>,
    Json(request): Json<CreateUserRequest>,
) -> Result<Json<UserResponse>, ApiError> {
    // Validate
    request.validate()
        .map_err(|e| ApiError::Validation(e))?;
    
    // Create user
    let user = state.user_service
        .create_user(request)
        .await?;
    
    Ok(Json(UserResponse::from(user)))
}
```

### Example 3: Database Repository

**Java (Spring Data):**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}
```

**Rust (SQLx):**
```rust
use sqlx::PgPool;

pub struct UserRepository {
    pool: PgPool,
}

impl UserRepository {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }
    
    pub async fn find_by_email(&self, email: &str) -> Result<Option<User>> {
        let user = sqlx::query_as!(
            User,
            "SELECT id, email, password, created_at FROM users WHERE email = $1",
            email
        )
        .fetch_optional(&self.pool)
        .await?;
        
        Ok(user)
    }
    
    pub async fn exists_by_email(&self, email: &str) -> Result<bool> {
        let exists = sqlx::query!(
            "SELECT EXISTS(SELECT 1 FROM users WHERE email = $1) as exists",
            email
        )
        .fetch_one(&self.pool)
        .await?
        .exists
        .unwrap_or(false);
        
        Ok(exists)
    }
}
```

---

## 🐳 Docker Configuration

### Dockerfile for Rust Service
```dockerfile
# Build stage
FROM rust:1.75-slim as builder

WORKDIR /app

# Copy workspace files
COPY Cargo.toml Cargo.lock ./
COPY shared ./shared
COPY services/user-service ./services/user-service

# Build in release mode
RUN cargo build --release --package user-service

# Runtime stage
FROM debian:bookworm-slim

RUN apt-get update && apt-get install -y \
    ca-certificates \
    libpq5 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy binary from builder
COPY --from=builder /app/target/release/user-service /app/user-service

# Copy configuration
COPY services/user-service/config ./config

EXPOSE 8080

CMD ["/app/user-service"]
```

### Updated docker-compose.yml
```yaml
services:
  user-service-rust:
    build:
      context: ./study-microservice-rust
      dockerfile: services/user-service/Dockerfile
    ports:
      - "8081:8080"
    environment:
      - DATABASE_URL=postgresql://admin:password@postgres:5432/user_db
      - RUST_LOG=info
    depends_on:
      - postgres
```

---

## 📚 Learning Resources

### Must-Read Books
1. **"The Rust Programming Language"** (The Book) - Free online
2. **"Rust for Rustaceans"** by Jon Gjengset - Advanced patterns
3. **"Zero To Production In Rust"** by Luca Palmieri - Web development

### Essential Tutorials
- [Axum Documentation](https://docs.rs/axum/latest/axum/)
- [SQLx GitHub Examples](https://github.com/launchbadge/sqlx)
- [Tokio Tutorial](https://tokio.rs/tokio/tutorial)
- [RabbitMQ with Rust (Lapin)](https://github.com/amqp-rs/lapin)

### Youtube Channels
- **Jon Gjengset** - Rust streams and deep dives
- **Let's Get Rusty** - Rust tutorials
- **No Boilerplate** - Quick Rust concepts

---

## 🎯 Migration Strategy Options

### Option 1: Parallel Migration (Recommended for Learning)
```
Keep Java services running → Build Rust equivalents → Test side-by-side → Switch traffic
```

**Pros:**
- Zero downtime
- Can compare implementations
- Gradual learning curve
- Easy rollback

**Cons:**
- Runs both stacks temporarily
- More infrastructure cost

### Option 2: Incremental Replacement
```
Replace one service → Verify → Replace next service → Repeat
```

**Pros:**
- Focused learning
- Lower resource usage
- Clear milestones

**Cons:**
- Mixed codebase for long time
- More complex service mesh

### Option 3: Fresh Start
```
Build entire Rust stack from scratch → Complete migration
```

**Pros:**
- Clean architecture
- Optimized for Rust patterns
- No Java baggage

**Cons:**
- High risk
- Long development time
- Loss of working system

---

## ✅ Migration Checklist Per Service

### Pre-Migration
- [ ] Understand current service's business logic
- [ ] Document all API endpoints
- [ ] List all dependencies and integrations
- [ ] Identify shared code/utilities
- [ ] Review database schema

### During Migration
- [ ] Set up Rust project structure
- [ ] Implement data models
- [ ] Set up database connections
- [ ] Implement repository layer
- [ ] Implement business logic
- [ ] Implement REST handlers
- [ ] Add input validation
- [ ] Implement error handling
- [ ] Add logging/tracing
- [ ] Write unit tests
- [ ] Write integration tests

### Post-Migration
- [ ] Performance testing
- [ ] Load testing
- [ ] Compare with Java version
- [ ] Update documentation
- [ ] Deploy to staging
- [ ] Monitor for issues
- [ ] Switch production traffic

---

## 📊 Expected Outcomes

| Metric | Java (Current) | Rust (Expected) |
|--------|----------------|-----------------|
| **Startup Time** | 5-10 seconds | < 1 second |
| **Memory Usage** | 200-500 MB | 20-50 MB |
| **Binary Size** | 50-100 MB (JAR) | 10-20 MB |
| **Request Latency** | p95: 50ms | p95: 10-20ms |
| **Throughput** | 5k req/s | 15-25k req/s |
| **Build Time** | 30 seconds | 1-3 minutes |

---

## 🚀 Quick Start Guide for First Service

### 1. Install Rust
```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

### 2. Create Workspace
```bash
mkdir study-microservice-rust
cd study-microservice-rust
cargo init --name workspace
```

### 3. Start with User Service
```bash
cargo new --bin services/user-service
cd services/user-service
cargo add axum tokio sqlx serde tracing
```

### 4. Run Locally
```bash
cargo run --bin user-service
```

---

## 💡 Pro Tips

1. **Start Small**: Don't migrate everything at once
2. **Use `cargo-watch`**: Auto-rebuild on file changes
   ```bash
   cargo install cargo-watch
   cargo watch -x run
   ```
3. **Use `clippy`**: Rust linter for best practices
   ```bash
   cargo clippy
   ```
4. **Profile Performance**: Use `cargo-flamegraph` to find bottlenecks
5. **Read Error Messages**: Rust compiler errors are helpful!
6. **Use `cargo-expand`**: See what macros generate
7. **Test Incrementally**: Write tests as you go

---

## 📅 Suggested Timeline

| Phase | Duration | Services | Goal |
|-------|----------|----------|------|
| **Phase 0** | Week 1 | - | Learn Rust basics, set up workspace |
| **Phase 1** | Week 2-3 | Notification Service | First working Rust microservice |
| **Phase 2** | Week 4-5 | User Service | SQL, JWT, validation |
| **Phase 3** | Week 6-7 | Product Service | Redis, caching patterns |
| **Phase 4** | Week 8-9 | Order Service | Service-to-service calls |
| **Phase 5** | Week 10 | Payment Service | External APIs |
| **Phase 6** | Week 11 | API Gateway | Complete migration |
| **Phase 7** | Week 12 | - | Testing, optimization |

**Total Timeline:** ~3 months

---

## 🎓 Next Steps

1. **Review this plan** and adjust based on your Rust experience
2. **Set up development environment** (Rust, IDE with rust-analyzer)
3. **Read "The Rust Book"** chapters 1-10 minimum
4. **Build a simple Axum hello-world** to understand basics
5. **Start with Notification Service** migration
6. **Update [ENHANCEMENTS_TODO.md](ENHANCEMENTS_TODO.md)** with Rust-specific tasks

---

*This migration plan is designed for learning. In production, carefully evaluate if Rust is the right choice for your use case.*
