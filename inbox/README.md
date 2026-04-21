# 📥 Agent Inbox

This directory holds **incoming task requests** for each agent.

When `@PMO` delegates a task, it creates a file here:
`inbox/{AGENT_NAME}/TASK-XXX.md`

## Structure
```
inbox/
├── PMO/          ← External requests waiting for orchestration
├── BA/           ← BA analysis requests
├── Tech-lead/    ← Architecture design requests
├── Java-BE/      ← Backend implementation requests
├── React-FE/     ← Frontend implementation requests
├── Code-Review/  ← Code review requests
├── QA-Tester/    ← Test execution requests
├── DB-Admin/     ← Schema design requests
└── DevOps-Engine/← CI/CD configuration requests
```

## Inbox File Format

Each `TASK-XXX.md` in an agent's inbox follows this structure:

```markdown
# Task: {short title}
**Task ID**: TASK-XXX
**From**: @{sender_agent}
**To**: @{receiver_agent}
**Priority**: HIGH | MEDIUM | LOW
**Created**: {ISO datetime}
**Status**: PENDING | IN_PROGRESS | COMPLETED

---
## Context
{Background information the agent needs}

## Deliverables
{Exact list of what must be produced}

## Input Files
- {file paths the agent should read}

## Output Location
- {where the agent should write results}

## Acceptance Criteria
- [ ] {criterion 1}
- [ ] {criterion 2}
```

## Rules
- An agent picks up tasks from its own inbox folder only.
- After completing a task, the agent moves the file to `outbox/{AGENT_NAME}/` and updates status to `COMPLETED`.
- @PMO monitors all inboxes to track overall progress.
