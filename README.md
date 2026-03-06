# Study Microservice

Study Microservice is a learning workspace for building and evolving a Spring Boot microservices architecture around an e-commerce domain.

## Overview

- Java 17, Spring Boot 3, Spring Cloud, and Gradle
- Infrastructure services for centralized config, service discovery, and gateway routing
- Implemented business-service code for users, products, and orders
- Doc-first or planned areas for payments, notifications, and broader end-to-end flows
- Shared design and delivery artifacts under `docs/`

## Repository Areas

- `api-gateway/` request routing, filters, and gateway concerns
- `config-server/` centralized configuration backed by `config-repo/`
- `discovery-service/` Eureka-based service discovery
- `user-service/` authentication and profile management
- `product-service/` product catalog and caching
- `order-service/` order orchestration and downstream service calls
- `docs/` architecture notes, task tracking, feature docs, and API docs
- `z-init-db/` database bootstrap scripts

## Documentation

- [Architecture](docs/Architecture.md)
- [Tasks](docs/Tasks.md)
- [Feature Docs](docs/feature-docs)
- [API Docs](docs/api-docs)

## Current State

The repository has working infrastructure and several implemented services, but it is not yet a fully uniform end-to-end system. Some areas are documented ahead of implementation, so use `docs/Architecture.md` and `docs/Tasks.md` as the current source of truth for scope and status.
