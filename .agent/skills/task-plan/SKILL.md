---
name: task-plan
description: Use after a tracked task from docs/Tasks.md has already been chosen and the user wants a pre-implementation plan. Analyze the selected tracked task, inspect relevant code and docs, produce an implementation plan and test plan, then stop for proceed or cancel. Do not use for task selection, coding, or completion reporting.
---
# Task Plan

Use this as the strict planning gate for tracked tasks.

This is the task-execution planning step in the workflow: plan first, then wait for approval before implementation.

## Goal

- Analyze the selected task from `docs/Tasks.md`
- Inspect architecture, code, feature docs, and API docs relevant to that task
- Produce a task-specific implementation plan
- Produce a task-specific test plan
- Stop and ask the user whether to `proceed`, `cancel`, or refine requirements

## Required Inputs

- The chosen task heading from `docs/Tasks.md`
- `README.md`
- `docs/Architecture.md`
- relevant code and documentation for the target service or feature

## Workflow

1. Re-read the selected task in `docs/Tasks.md`.
2. Use `read-project` to ground in the repository and affected services.
3. Inspect code, configs, tests, and existing docs related to the task.
4. Decide whether the task changes APIs or contracts.
5. Produce two explicit sections:
   - `Implementation Plan`
   - `Test Plan`
6. End by asking the user to choose one of:
   - `proceed`
   - `cancel`
   - refine requirements

## Planning Output Requirements

The plan must be specific enough to implement without further design work.

### Implementation Plan

Include:

- affected services or subsystems
- concrete code changes
- config or data changes
- documentation changes
- whether `document-feature`, `api-docs`, and `implement-feature` are needed, and in what order

### Test Plan

Include:

- relevant unit, integration, or service-level tests
- smoke checks for the touched behavior
- any validation gaps or setup constraints

## API or Contract Rule

If the task changes an API, message contract, or externally visible behavior, route the workflow in this order:

1. `document-feature`
2. `api-docs`
3. `implement-feature`

If the task does not change APIs or contracts, go directly to `implement-feature` after approval.

## Guardrails

- Do not implement the task inside this skill.
- Do not update `docs/Tasks.md` here.
- Treat this as a behavioral planning gate; it does not assume platform-level mode switching.
