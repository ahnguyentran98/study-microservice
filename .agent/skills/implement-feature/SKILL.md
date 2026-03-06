---
name: implement-feature
description: Use after task-plan approval to implement a tracked task in code, and after document-feature/api-docs when the approved plan requires API documentation updates.
---

# Implement Feature

Use this as the code-change helper behind the task workflow.

## Entry Condition

- The task has already been selected by `task-check`.
- The task has already been analyzed and approved by `task-plan`.
- If the task changes API behavior, `document-feature` and `api-docs` have already run or are being updated in the same workflow.

## Before Coding

1. Re-read the approved task in `docs/Tasks.md`.
2. Re-read the task-specific implementation plan and test plan.
3. Find related code in the target service.
4. Check for existing feature docs and Postman collections when relevant.
5. Identify dependencies, cross-service calls, config touchpoints, and existing tests.

## Investigation Checklist

- Search for similar endpoints, DTOs, services, repositories, and exception handling.
- Inspect config before adding new properties.
- Check whether the feature or partial implementation already exists.
- Verify how the service handles REST clients, messaging, validation, and tests.

## Implementation Order

1. Model or entity changes
2. Repository changes
3. DTOs
4. Service logic
5. Controller endpoints
6. Config updates
7. Tests

## Guardrails

- Follow `.agent/rules/project-conventions.md`.
- Preserve service boundaries.
- Do not hardcode service addresses or secrets.
- Add timeouts and resilience patterns to external calls where applicable.
- Prefer incremental edits over broad rewrites.
- Do not mark a task complete here; that belongs to `task-report`.
