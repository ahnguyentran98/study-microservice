# AGENTS.md

## Repository role

This repository is a Spring Boot microservices workspace.
Treat each service as an independent bounded context with its own data, contracts, and runtime concerns.

## Instruction priority

Follow this file for repository-wide behavior.
Use project skills for repeatable workflows.
When a workflow skill applies, prefer using the skill instead of improvising the process.

## Task workflow routing

When the user asks to:
- choose the next tracked task
- list open tracked tasks
- check tracked task status
- validate a named tracked task from `docs/Tasks.md`
- decide which tracked task should move to planning next

use the `task-check` skill first.

After a task is chosen, use the workflow in this order:
1. `task-plan`
2. if API or contract changes are involved:
   - `document-feature`
   - `api-docs`
3. `implement-feature`
4. `task-report`

Do not skip `task-plan` before implementation.
Do not mark tasks complete outside `task-report`.

## Source of truth

Use these files as the main project truth in this order:
1. `README.md`
2. `docs/Architecture.md`
3. `docs/Tasks.md`
4. relevant files under `docs/feature-docs/`
5. relevant files under `docs/api-docs/`

Do not assume implementation status from old plans alone.
Confirm current behavior from code and current docs.
