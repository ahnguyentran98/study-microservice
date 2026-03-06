---
trigger: always_on
description: Always-on project conventions for the study-microservice workspace
---

# Project Conventions

Apply these rules to code changes in this repository.

## Scope

- Treat this repository as a Spring Boot microservices system.
- Respect service boundaries. Each service owns its own data and contracts.
- Prefer changes that fit the existing architecture before introducing new patterns.

## Implementation Expectations

- Investigate existing code before adding new classes, endpoints, or configs.
- Reuse established package structure: `config`, `controller`, `service`, `repository`, `entity`, `dto`, `exception`.
- Add or update tests when behavior changes.
- Keep comments brief and only for non-obvious logic.

## Naming

- Use `camelCase` for variables and methods.
- Use `PascalCase` for classes and interfaces.
- Use `UPPER_SNAKE_CASE` for constants.
- Prefer method names like `getOrderById`, `createOrder`, `updateStatus`, `deleteOrder`.

## REST Conventions

- Use resource-oriented paths such as `/api/v1/orders` and `/api/v1/orders/{id}`.
- Match HTTP methods to CRUD semantics.
- Do not encode actions into URLs when standard resource routes are sufficient.

## Microservice Boundaries

- Do not access another service's database directly.
- Do not share entity classes across services.
- Use REST or messaging for cross-service communication.
- Do not hardcode service URLs; use discovery/config-driven resolution.

## Resilience

- External service calls should have explicit timeouts.
- Use the project's resilience patterns for cross-service calls when applicable.
- Design async consumers to be idempotent.
- Do not assume synchronous guarantees from message delivery.

## Configuration

- Keep secrets out of committed config.
- Prefer externalized configuration over hardcoded environment values.
- Preserve Config Server, Eureka, and Gateway integration patterns already used in the repo.
