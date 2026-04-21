# 📤 Agent Outbox

This directory holds **completed artifacts** from each agent.

When an agent finishes a task, it places its output here:
`outbox/{AGENT_NAME}/TASK-XXX/`

## Structure
```
outbox/
├── BA/             ← Feature specs, user stories
├── Tech-lead/      ← Architecture docs, API specs
├── Java-BE/        ← Backend source code bundles, implementation notes
├── React-FE/       ← Frontend component bundles, implementation notes
├── Code-Review/    ← Review reports (LGTM / CHANGES_REQUIRED)
├── QA-Tester/      ← Test reports (ALL_TESTS_PASSED / TESTS_FAILED)
├── DB-Admin/       ← Migration files, query optimization reports
└── DevOps-Engine/  ← Dockerfile, CI/CD configs, deployment scripts
```

## Outbox File Format

Each task output directory contains:

```
outbox/{AGENT_NAME}/TASK-XXX/
├── summary.md         ← What was done, what was produced
├── {artifact files}   ← The actual deliverables
└── handoff.json       ← Structured handoff metadata (mirrors LOGS.json format)
```

### `handoff.json` format:
```json
{
  "task_id": "TASK-XXX",
  "from": "@{agent}",
  "to": "@{next_agent}",
  "status": "COMPLETED",
  "timestamp": "{ISO8601}",
  "output_files": ["file1", "file2"],
  "notes": "Summary of what was done and any flags for the next agent"
}
```

## Rules
- All outputs **must** have a `summary.md` — no silent deliveries.
- `@PMO` reads the outbox to verify completion before updating `DASHBOARD.md`.
- Source code is committed to the main repo; this outbox holds documentation artifacts.
