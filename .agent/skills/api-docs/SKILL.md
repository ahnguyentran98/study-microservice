---
name: api-docs
description: Use after task-plan approval when a tracked task needs a Postman collection under docs/api-docs/<service-folder>/ for new or changed API behavior.
---

# API Docs

Use this as a supporting skill after `task-plan` approves work that changes API behavior and requires Postman documentation.

## Entry Condition

- The task has already been selected by `task-check`.
- The task has already been analyzed by `task-plan`.
- The related feature doc already exists or is created in the same workflow.

## Input

- Source feature doc in `docs/feature-docs/<service-folder>/<operation-name>.md`

## Output

- `docs/api-docs/<service-folder>/<operation-name>.postman_collection.json`

## Workflow

1. Read the approved task and the related feature doc.
2. Extract method, path, auth requirements, request body, params, and responses.
3. Build or update the Postman collection with realistic examples.
4. Include success and representative error responses.

## Collection Conventions

- Use `{{baseUrl}}` for the gateway base URL.
- Add `{{token}}` when the endpoint is authenticated.
- Keep collections scoped to one feature or operation unless the task explicitly groups related requests.
- Store files under the service folder, not flat under `docs/api-docs/`.

## Minimum Contents

- `info`
- `variable` with `baseUrl` and optional `token`
- `item` requests
- Example response bodies for main success and common failure cases

## Guardrails

- Do not use this skill for task selection or task approval.
- Keep the collection aligned with the approved feature doc and actual implementation.
