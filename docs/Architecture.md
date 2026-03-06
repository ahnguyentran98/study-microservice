# Architecture

This document describes the current repository architecture and project structure.

## System Overview

- Domain: e-commerce learning platform
- Backend stack: Java 17, Spring Boot 3, Spring Cloud, Gradle
- Core infrastructure: Config Server, Eureka Discovery, API Gateway
- Implemented business services: `user-service`, `product-service`, `order-service`
- Planned or doc-first areas: `payment-service`, `notification-service`
- Supporting assets: `config-repo`, `z-init-db`, `docs/feature-docs`, `docs/api-docs`

## Repository Structure

```text
study-microservice/
├── README.md
├── docs/
│   ├── Architecture.md
│   ├── Tasks.md
│   ├── feature-docs/
│   └── api-docs/
├── api-gateway/
├── config-server/
├── discovery-service/
├── user-service/
├── product-service/
├── order-service/
├── payment-service/
├── notification-service/
├── config-repo/
└── z-init-db/
```

## Component Status

| Component | State | Notes |
|-----------|-------|-------|
| `api-gateway/` | Implemented | Gateway entry point with routing, filters, and resilience concerns |
| `config-server/` | Implemented | Serves externalized properties from `config-repo/` |
| `discovery-service/` | Implemented | Eureka server for service registration and lookup |
| `user-service/` | Implemented | Authentication and profile management service |
| `product-service/` | Implemented | Product catalog service with Redis integration |
| `order-service/` | Implemented | Order flow, downstream clients, RabbitMQ publisher, and saga-oriented logic |
| `payment-service/` | Placeholder | Directory exists, but application source is not present yet |
| `notification-service/` | Placeholder | Directory exists, but application source is not present yet |
| `docs/feature-docs/` | Active | Endpoint-level design docs, including planned services |
| `docs/api-docs/` | Active | Postman collections that mirror feature docs |

## Runtime Shape

### Request Flow

```text
Client -> API Gateway -> Versioned service endpoints
```

### Infrastructure Flow

```text
Service startup -> Config Server -> Discovery Service registration -> Request handling
```

### Data and Messaging

- `user-service` uses PostgreSQL-backed user data.
- `product-service` uses PostgreSQL with Redis for caching.
- `order-service` uses JPA entities plus downstream product/payment clients and RabbitMQ publishing.
- Payment and notification flows are documented, but those services are not yet implemented in this workspace.

## Project Structure Notes

- There is no single root `docker-compose.yml` in the repository.
- Local orchestration is currently service-specific, for example:
  - `api-gateway/docker-compose.yml`
  - `config-server/docker-compose.yml`
  - `discovery-service/docker-compose.yml`
  - `user-service/docker-compose.yml`
  - `product-service/docker-compose.yml`
- `config-repo/` currently contains shared and service-specific properties for the implemented services only.
- `z-init-db/` currently contains bootstrap SQL for the separated user and product databases.
- A frontend application is described in older design material, but there is no `frontend/` source directory in the current repo.

## Service Notes

### `api-gateway/`

- Main entry point for external requests
- Handles route mapping, request filtering, and cross-cutting concerns

### `config-server/`

- Centralizes Spring configuration
- Reads from the local `config-repo/` directory

### `discovery-service/`

- Hosts Eureka
- Enables discovery-based routing and lookup across services

### `user-service/`

- Owns authentication and profile data
- Includes security, validation, and error-handling layers

### `product-service/`

- Owns product catalog behavior
- Includes CRUD, search, and caching concerns

### `order-service/`

- Owns order creation, retrieval, and cancellation flows
- Includes saga-style orchestration and downstream product/payment clients

## Working Assumption

When documentation and source code disagree, prefer the source tree and service configuration files. This architecture document is meant to stay aligned with the repository as it exists today, not with older target-state plans.
