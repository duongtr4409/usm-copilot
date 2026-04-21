# Code Review Reports
**Owner**: @Code-Review
**Consumer**: @PMO (gate decision), @Java-BE / @React-FE (fixes)

This directory holds all code review reports.

## Naming Convention
```
TASK-{number}-{BE|FE}-review.md
```

Examples:
- `TASK-001-BE-review.md`
- `TASK-001-FE-review.md`

## Verdict: LGTM
When @Code-Review issues `LGTM`, @PMO:
1. Updates `TASK_BOARD.md` to `IN_QA`
2. Triggers `@QA-Tester` to execute tests
3. Appends to `LOGS.json`

## Verdict: CHANGES_REQUIRED
When @Code-Review issues `CHANGES_REQUIRED`, @PMO:
1. Returns the task to the relevant dev agent
2. Dev fixes issues and resubmits
3. @Code-Review performs a second review (focused diff only)
4. Metrics are tracked in `DASHBOARD.md`
