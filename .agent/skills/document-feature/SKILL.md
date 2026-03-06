---
name: document-feature
description: Use after task-plan approval when a tracked task changes API behavior or contracts and needs a feature doc under docs/feature-docs/<service-folder>/.
---

# Document Feature

Use this as a supporting skill after `task-plan` approves work that changes an API, contract, or externally visible feature flow.

## Entry Condition

- The task has already been selected by `task-check`.
- The task has already been analyzed by `task-plan`.
- The task plan identified a need for new or updated feature documentation.

## Output

- Create or update `docs/feature-docs/<service-folder>/<operation-name>.md`
- Keep one file per endpoint or operation

Examples:

- `docs/feature-docs/user-service/register-user.md`
- `docs/feature-docs/order-service/update-order-status.md`

## Workflow

1. Re-read the chosen task in `docs/Tasks.md`.
2. Inspect existing code and feature docs for similar behavior.
3. Write a focused feature doc for the exact operation being changed.
4. Include acceptance criteria and realistic request/response examples.
5. If the task spans multiple endpoints, create one feature doc per endpoint instead of combining them.

## Required Sections

- Purpose
- Service
- API endpoint
- Request body or params
- Response
- Acceptance criteria

## Guardrails

- Do not use this skill to choose the task; that belongs to `task-check`.
- Do not use this skill to approve implementation; that belongs to `task-plan`.
- Match the real service name and route conventions used by the repository.
- Keep examples implementation-ready rather than aspirational.
