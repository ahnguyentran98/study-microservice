---
name: task-report
description: Use after task implementation when the user wants to verify a task, mark a task complete, or update docs/Tasks.md after the relevant tests and smoke checks pass.
---

# Task Report

Use this as the final step in the task workflow.

## Goal

- Verify that the selected task was implemented successfully
- Run relevant tests and smoke checks for the touched service or feature
- Report success, failure, or verification gaps
- Update `docs/Tasks.md` only when the task passes the required validation

## Verification Scope

Use relevant verification only, not full-repo validation by default.

Examples:

- service-specific tests for the changed module
- targeted integration tests
- smoke checks for the changed endpoint, flow, or config

## Workflow

1. Identify the selected task by its heading in `docs/Tasks.md`.
2. Review the implementation changes and the `task-plan` test plan.
3. Run the relevant tests and smoke checks for the touched area.
4. Classify the result:
   - passed
   - failed
   - blocked or incomplete verification
5. Update `docs/Tasks.md` only if the required verification passed.

## Tasks File Update Rule

When verification passes:

- move the task into `## Completed`
- convert the heading to `[x]`
- keep the completed entry concise
- add a short completion summary instead of copying the full original task block

When verification fails or cannot be completed:

- leave task status unchanged
- report the failure, missing checks, or blockers clearly

## Guardrails

- Do not close a task based on code changes alone; required verification must pass.
- Do not broaden verification to the full repository unless the task specifically requires it.
- Keep `docs/Tasks.md` aligned with observed test results, not assumptions.
