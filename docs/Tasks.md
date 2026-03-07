# Tasks

This file is the task board for the repository. Update statuses and notes as work lands.

## Status Legend

- `[x]` completed
- `[ ]` open or still in progress

## Completed

### [x] 1.1 Health checks

Actuator health endpoints and dependency visibility are in place for the implemented services.

### [x] 1.2 Centralized configuration

Config Server and `config-repo/` are part of the current workspace.

### [x] 1.3 Service discovery

Eureka-based discovery is implemented through `discovery-service/`.

### [x] 1.4 Circuit breaker

Resilience patterns have been introduced for downstream calls.

### [x] 2.1 Global exception handling

Implemented services use standardized error handling.

### [x] 2.2 Correlation IDs

Correlation IDs are propagated through the gateway and services.

### [x] 2.3 API versioning

Versioned API paths under `/api/v1/` are in use.

### [x] 2.4 Input validation

Bean Validation and validation error responses are wired into the implemented APIs.

### [x] 2.5 Database-per-service rollout

`order-service` now loads its database settings from `config-repo/`, ships runnable compose/bootstrap assets, and the shared DB/bootstrap naming is aligned across the implemented services. Placeholder config and init scripts were also added for future `payment-service` and `notification-service`.

## In Progress

### [ ] 3.1 Saga pattern hardening

Current state:
- `order-service` already contains create/cancel orchestration with inventory compensation and payment refund hooks

Remaining work:
- Add explicit saga step or state tracking beyond `OrderStatus`
- Tighten error semantics around rollback paths, especially partial compensation failures
- Expand automated test coverage beyond the current context-load smoke test

## Backlog

### [ ] 3.2 Event sourcing (lite)

- Add an order-event history model
- Persist state transitions for replay or audit
- Expose order history from event data

### [ ] 3.3 CQRS (simple)

- Separate write behavior from read-optimized product or order queries
- Introduce a read model or projection where it materially helps

### [ ] 3.4 Distributed tracing

- Add a tracing backend such as Zipkin
- Connect gateway and services to trace collection
- Move from correlation IDs alone to full request timelines

### [ ] F.1 Basic frontend application

- Create a small UI that talks to the API Gateway
- Support register, login, profile, product list, product detail, and search
- Keep it minimal and use the versioned API paths already exposed by the backend

## Suggested Order

1. Finish database separation across all services
2. Harden the saga implementation in `order-service`
3. Add distributed tracing once request flow is stable
4. Add a minimal frontend when the core APIs are stable enough to demo end-to-end
5. Explore event sourcing or CQRS only after the base platform is consistent

## Maintenance Notes

- Prefer updating this file instead of creating new roadmap docs at the repo root.
- Use concise status notes tied to the actual source tree, not aspirational architecture.
- If a task becomes large, link its supporting design or feature docs from `docs/feature-docs/`.

*Last updated: 2026-03-07*
