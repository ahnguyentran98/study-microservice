---
trigger: always_on
description: Always-on project conventions for the study-microservice workspace
---

## Project conventions

### Scope

- Respect service boundaries.
- Each service owns its own database and contracts.
- Prefer changes that fit the existing architecture before introducing new patterns.
- Preserve existing Gateway, Eureka, and Config Server integration patterns already used in the repo.

### Investigation before change

- Investigate existing code before adding new classes, endpoints, configs, or infrastructure.
- Reuse existing package structure before creating new structural patterns.
- Check whether similar endpoints, DTOs, services, repositories, exceptions, configs, or tests already exist.
- Prefer incremental edits over broad rewrites.

### Package structure

Prefer the established package layout when applicable:

- `config`
- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `exception`

### Naming

- Use `camelCase` for variables and methods.
- Use `PascalCase` for classes and interfaces.
- Use `UPPER_SNAKE_CASE` for constants.
- Prefer method names such as `getOrderById`, `createOrder`, `updateStatus`, and `deleteOrder`.

## REST conventions

- Use resource-oriented paths such as `/api/v1/orders` and `/api/v1/orders/{id}`.
- Match HTTP methods to CRUD semantics.
- Do not encode actions into URLs when standard resource routes are sufficient.
- Keep request and response contracts aligned with existing route conventions in the target service.

## Microservice boundaries

- Do not access another service's database directly.
- Do not share entity classes across services.
- Use REST or messaging for cross-service communication.
- Do not hardcode service URLs; use discovery- or config-driven resolution.

## Resilience

- External service calls must use explicit timeouts.
- Use existing project resilience patterns for cross-service communication when applicable.
- Design async consumers to be idempotent.
- Do not assume synchronous guarantees from message delivery.

## Configuration

- Keep secrets out of committed config.
- Prefer externalized configuration over hardcoded environment values.
- Inspect existing config before introducing new properties.
- Preserve current integration patterns for service discovery, centralized config, messaging, and gateway routing.

## Testing and verification

- Add or update tests when behavior changes.
- Prefer targeted verification for the touched service or feature instead of full-repo validation by default.
- Before reporting a task complete, run the checks required by the approved task plan.
- Do not claim success without stating what was verified.

## Documentation expectations

- If a task changes an API, contract, or externally visible behavior, update feature documentation and API documentation as required by the task workflow.
- Keep docs aligned with actual implementation, not intended behavior.
- Keep one feature doc per endpoint or operation unless the task explicitly groups related requests.

## Code style

- Keep comments brief and only for non-obvious logic.
- Preserve service boundaries and existing architectural intent.
- Avoid unnecessary abstractions and avoid speculative refactors.

## Response behavior

- Be explicit about assumptions, blockers, and unknowns.
- When task selection is requested, do not start implementation directly.
- When planning is requested, produce an implementation plan and test plan, then wait for approval.
- When verification fails or is incomplete, report the gap clearly and do not mark the task complete.
