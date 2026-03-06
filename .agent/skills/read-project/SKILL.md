---
name: read-project
description: Use only as an internal repository-orientation step before task-check, task-plan, or implement-feature when repository structure, affected services, or existing docs are still unclear. Do not use as the main response for ordinary coding, planning, or reporting requests.
---

# Read Project

Use this as the orientation skill behind the task workflow.

## Primary Use

- `task-check` uses this skill to understand the current repository scope.
- `task-plan` uses this skill before building a task-specific plan.
- `implement-feature` can use this skill when code context is still unclear.

## Read in This Order

1. `README.md`
2. `docs/Architecture.md`
3. `docs/Tasks.md`
4. Relevant files in `docs/feature-docs/` or `docs/api-docs/` when the chosen task maps to a feature

## Meaning of the Main Docs

- `README.md`: top-level overview only
- `docs/Architecture.md`: current architecture and project structure
- `docs/Tasks.md`: task source of truth and status tracking

## Service Inspection

For a target service, inspect these first:

- `build.gradle`
- `src/main/resources/application.properties` or `bootstrap.properties`
- `src/main/java/**/<Service>Application.java`
- `src/main/java/**/controller/*`
- `src/main/java/**/service/*`
- `src/main/java/**/repository/*`
- `src/main/java/**/entity/*`
- local `docker-compose.yml` if the service has one

## Fast Search Patterns

- Controllers: `rg --files <service> | rg 'Controller\\.java$'`
- Services: `rg --files <service> | rg 'Service\\.java$'`
- Repositories: `rg --files <service> | rg 'Repository\\.java$'`
- Inter-service REST: `rg -n "WebClient|RestTemplate|Feign" <service>/src/main/java`
- Messaging: `rg -n "@RabbitListener|RabbitTemplate" <service>/src/main/java`
- Config usage: `rg -n "spring.application.name|spring.cloud.config|eureka|rabbitmq|redis|datasource" <service>/src/main/resources`

## Guardrail

Do not infer implementation status from older plans or stale docs. Confirm the current state from the codebase and the docs under `docs/`.
