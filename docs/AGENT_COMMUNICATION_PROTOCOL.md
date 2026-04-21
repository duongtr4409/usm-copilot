# Agent Communication Protocol (ACP)
**Version**: 1.0.0
**Author**: @PMO
**Last Updated**: 2026-04-21

> This document defines how all agents communicate with each other.
> Every agent MUST follow this protocol without exception.

---

## 1. Core Principle: State-Based Communication

Agents do **not** communicate via direct chat. Instead, they:
1. **Write** outputs to shared files (docs, code, LOGS.json)
2. **Read** inputs from shared files (specs, architecture, API contract)
3. **Signal** completion via a LOGS.json transaction entry

This prevents "lost in conversation" drift and creates a full audit trail.

---

## 2. Hand-off Syntax

When `@PMO` delegates work, the instruction follows this format:

```
@{AgentName}: "{Clear action description}"

Context:
  Task ID    : TASK-XXX
  Input      : {list of files the agent must read}
  Output     : {deliverables and their locations}
  Deadline   : {sprint milestone or date}
  Gate       : {who approves completion}
```

### Example — PMO → BA
```
@BA: "Analyze the following requirements and produce a feature spec."

Context:
  Task ID    : TASK-001
  Input      : User request: "Build a user login and registration system with JWT"
  Output     : docs/specs/TASK-001-auth.md
  Gate       : @PMO must approve spec before @Tech-lead proceeds
```

### Example — PMO → parallel agents
```
@Java-BE: "Implement the Auth module backend."
@React-FE: "Implement the Auth module frontend."
@QA-Tester: "Prepare test scenarios for Auth."

  (All three run simultaneously)

Context:
  Task ID    : TASK-001
  Input      : ARCHITECTURE.md, API_SPEC.yaml, docs/specs/TASK-001-auth.md
  Output     :
    @Java-BE   → src/main/java/com/usm/auth/...
    @React-FE  → src/features/auth/...
    @QA-Tester → docs/test-scenarios/TASK-001-scenarios.md
  Gate       : @Code-Review (after both BE and FE complete)
```

---

## 3. LOGS.json Transaction Protocol

Every agent appends one entry to `.github/copilot-agents/LOGS.json` when:
- Receiving a task delegation
- Completing a task
- Escalating a blocker

### Entry Format
```json
{
  "timestamp": "ISO 8601 (e.g. 2026-04-21T15:30:00Z)",
  "from": "@{AgentName}",
  "to": "@{NextAgent}",
  "task_id": "TASK-XXX",
  "status": "DELEGATED | IN_PROGRESS | COMPLETED | LGTM | ALL_TESTS_PASSED | BLOCKED | FAILED",
  "input": {
    "key": "value — what was received"
  },
  "output": {
    "key": "value — what was produced"
  },
  "log": "Human-readable summary of what happened and any important notes."
}
```

### Status Definitions

| Status | Meaning | Who Sets It |
|---|---|---|
| `DELEGATED` | @PMO sent to an agent | @PMO |
| `IN_PROGRESS` | Agent has started | The receiving agent |
| `COMPLETED` | Agent finished successfully | The receiving agent |
| `LGTM` | Code review passed | @Code-Review only |
| `ALL_TESTS_PASSED` | QA verified | @QA-Tester only |
| `CHANGES_REQUIRED` | Review failed — must redo | @Code-Review only |
| `TESTS_FAILED` | QA failed — must fix | @QA-Tester only |
| `BLOCKED` | Agent cannot proceed | Any agent |

---

## 4. Approval Gate Protocol

### Gate: Design approval (PMO → Tech-lead)
```
CONDITION: @BA spec is APPROVED by @PMO
ACTION:    @PMO sets TASK-XXX → IN_PROGRESS (Design) in TASK_BOARD.md
           @PMO writes LOGS.json entry: status=DELEGATED to=@Tech-lead
```

### Gate: LGTM (Code-Review → PMO)
```
CONDITION: @Code-Review finds ZERO Critical + ZERO Major issues
ACTION:    @Code-Review writes LOGS.json entry: status=LGTM
           @Code-Review saves review report to docs/review-reports/
           @PMO reads LGTM → triggers @QA-Tester
```

### Gate: ALL_TESTS_PASSED (QA-Tester → PMO)
```
CONDITION: All test cases in scenarios file pass
ACTION:    @QA-Tester writes LOGS.json entry: status=ALL_TESTS_PASSED
           @QA-Tester saves report to docs/test-reports/
           @PMO reads result → triggers @DevOps-Engine
           @PMO marks feature DONE in TASK_BOARD.md
           @PMO notifies user: "Feature TASK-XXX is ready to merge."
```

---

## 5. Escalation Protocol

If an agent is **blocked** (missing info, ambiguous requirement, tool failure):

1. Agent appends to LOGS.json:
```json
{
  "status": "BLOCKED",
  "from": "@{Agent}",
  "to": "@PMO",
  "log": "Blocked: {specific reason}. Need: {what is needed}."
}
```
2. @PMO reads the BLOCKED entry.
3. @PMO either:
   - Provides the missing information
   - Routes to another agent that has the answer
   - Escalates to the user

---

## 6. Universal Language Rules

| Artifact | Language |
|---|---|
| Source code (all languages) | English |
| Variable / function / class names | English |
| API field names / JSON keys | English |
| Database column names | English |
| Git commit messages | English |
| Code comments (logic explanation) | English |
| User-facing UI text | Product language (Vietnamese or English as configured) |
| Specs in `docs/specs/` | Can be Vietnamese (for business stakeholders) |
| LOGS.json `log` field | English |

---

## 7. Forbidden Communication Patterns

| ❌ Pattern | ✅ Correct Alternative |
|---|---|
| Agent writes code without reading API_SPEC.yaml | Always read API_SPEC.yaml first |
| Agent modifies another agent's owned file | Only the owner can modify (e.g., only @Tech-lead edits ARCHITECTURE.md) |
| Agent self-approves its own output | Always route through the defined gate-keeper |
| Agent skips LOGS.json entry | Every state change MUST be logged |
| @PMO allows merge without LGTM + ALL_TESTS_PASSED | Both gates are mandatory, no exceptions |
