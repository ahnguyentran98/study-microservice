---
name: task-check
description: Use when the user asks what task to implement next, wants to list tracked tasks, wants to check task status, or wants help choosing a task from docs/Tasks.md before planning implementation.
---

# Task Check

Use this as the first step in the task workflow.

## Goal

- Read `docs/Tasks.md` as the task source of truth.
- List all open tasks.
- Recommend the next task to work on.
- Ask the user which task to proceed with.
- Hand off to `task-plan` after a task is chosen.

## Task Ordering Rule

Recommend the first open task by file order using this priority:

1. First open task under `## In Progress`
2. If none are open there, first open task under `## Backlog`

Still list every open task so the user can choose a different one.

## Workflow

1. Use `read-project` to confirm the current repository scope if needed.
2. Parse open task headings from `docs/Tasks.md`.
3. Present:
   - recommended next task
   - all open tasks
   - a short reason for the recommendation
4. If the user already named a task, validate it against `docs/Tasks.md` and skip the selection prompt.
5. Once a task is chosen, continue with `task-plan`.

## Canonical Task Identifier

Use the task heading text from `docs/Tasks.md`, for example:

- `2.5 Database-per-service rollout`
- `3.1 Saga pattern hardening`

## Guardrails

- Do not mutate `docs/Tasks.md` when a task is only being selected.
- Do not produce an implementation plan here; that belongs to `task-plan`.
- If the user names a completed task, call that out explicitly before proceeding.
