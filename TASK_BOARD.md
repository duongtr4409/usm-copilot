# 📋 Task Board
**Manager**: @PMO
**Last Updated**: 2026-04-22T08:50:00Z
**Active Sprint**: — (Awaiting first request)

---

## 📊 Board Summary

| Status | Count |
|---|---|
| 🔵 BACKLOG | 3 |
| 🟡 TODO | 3 |
| 🔄 IN_PROGRESS | 1 |
| 🔍 IN_REVIEW | 0 |
| 🧪 IN_QA | 0 |
| ✅ DONE | 3 |
| ❌ BLOCKED | 0 |

---

## 🗂️ Active Features

---

## Feature: Academy Management System (AMS) - Initial Request
**Epic ID**: EPIC-AMS-001
**Requested**: 2026-04-21
**Priority**: HIGH
**Status**: 🔄 IN_PROGRESS

### Task Breakdown

| Task ID | Description | Owner | Status | Blocker | Updated |
|---|---|---|---|---|---|
| TASK-001 | Create PLAN & TASK IDs | @PMO | ✅ DONE | — | 2026-04-21 |
| TASK-002 | Business Requirements Analysis | @BA | ✅ DONE | TASK-001 | 2026-04-21 |
| TASK-003 | System Architecture Design | @Tech-lead | ✅ DONE | TASK-002 | 2026-04-21 |
| TASK-004 | Database Schema Design | @DB-Admin | ✅ DONE | TASK-003 | 2026-04-21 |
| TASK-005 | Backend Implementation | @Java-BE | 🟡 TODO | TASK-003, TASK-004 |  |
| TASK-006 | Frontend Implementation | @React-FE | 🟡 TODO | TASK-003 |  |
| TASK-007 | QA Test Preparation | @QA-Tester | ✅ DONE | TASK-002 | 2026-04-21 |
| TASK-008 | Code Review — BE & FE | @Code-Review | 🔵 BACKLOG | TASK-005, TASK-006 |  |
| TASK-009 | QA Test Execution | @QA-Tester | 🔵 BACKLOG | TASK-008 |  |
| TASK-010 | Implement AddStudentToClass | @Java-BE | ✅ DONE | TASK-003, TASK-004 | 2026-04-21 |
| TASK-011 | DevOps: Docker & Compose deployment | @DevOps-Engine | 🔄 IN_PROGRESS | TASK-003, TASK-004 | 2026-04-21 |
| TASK-011 | DevOps: Docker & Compose deployment | @DevOps-Engine | ✅ DONE | TASK-003, TASK-004 | 2026-04-21 |
| TASK-013 | Design RBAC & JWT | @Tech-lead | ✅ DONE | TASK-002 | 2026-04-21 |
| TASK-014 | Implement RBAC & JWT | @Java-BE | ✅ DONE | TASK-013 | 2026-04-21 |
| TASK-016 | Unblock integration tests (Testcontainers/Docker API and Flyway fixes) | @DevOps-Engine / @Java-BE | 🔄 IN_PROGRESS | TASK-012 | 2026-04-22 |
| TASK-017 | Fix compose test credential propagation | @Java-BE / @DevOps-Engine | 🔄 IN_PROGRESS | TASK-016 | 2026-04-22 |
| TASK-018 | Fix enrollment table/entity naming mismatch | @Java-BE | 🔄 IN_PROGRESS | TASK-017 | 2026-04-22 |
| TASK-019 | Relax security for integration-tests profile | @Java-BE | 🔄 IN_PROGRESS | TASK-018 | 2026-04-22 |

### Linked Documents
- Requirements: [inbox/wUser/myRequirement.md](inbox/wUser/myRequirement.md)
- Architecture: [ARCHITECTURE.md](ARCHITECTURE.md)
- API contract: [API_SPEC.yaml](API_SPEC.yaml)

---

---

## ✅ Completed Features

> *No completed features yet.*

---

## 📌 Template — How @PMO Populates This Board

When a new feature is received, @PMO adds a block like this:

```markdown
---

## Feature: {Feature Name}
**Epic ID**: EPIC-001
**Requested**: {date}
**Priority**: HIGH
**Status**: 🔄 IN_PROGRESS

### Task Breakdown

| Task ID | Description | Owner | Status | Blocker | Updated |
|---|---|---|---|---|---|
| TASK-001 | Business Requirements Analysis | @BA | ✅ DONE | — | {date} |
| TASK-002 | System Architecture Design | @Tech-lead | 🔄 IN_PROGRESS | TASK-001 | {date} |
| TASK-003 | Database Schema Design | @DB-Admin | 🟡 TODO | TASK-002 | {date} |
| TASK-004 | Backend Implementation | @Java-BE | 🟡 TODO | TASK-002, TASK-003 | {date} |
| TASK-005 | Frontend Implementation | @React-FE | 🟡 TODO | TASK-002 | {date} |
| TASK-006 | QA Test Preparation | @QA-Tester | 🔄 IN_PROGRESS | TASK-001 | {date} |
| TASK-007 | Code Review — BE | @Code-Review | 🔵 BACKLOG | TASK-004 | — |
| TASK-008 | Code Review — FE | @Code-Review | 🔵 BACKLOG | TASK-005 | — |
| TASK-009 | QA Test Execution | @QA-Tester | 🔵 BACKLOG | TASK-007, TASK-008 | — |
| TASK-010 | Infrastructure & CI/CD | @DevOps-Engine | 🔵 BACKLOG | TASK-009 | — |

### Approval Gates
| Gate | Approved By | Status |
|---|---|---|
| Requirements → Design | @PMO | ⏳ Pending |
| Design → Development | @PMO | ⏳ Pending |
| Code → Review | automatic | ⏳ Pending |
| Review → QA | @Code-Review (LGTM) | ⏳ Pending |
| QA → Merge | @QA-Tester (ALL_TESTS_PASSED) | ⏳ Pending |

### Linked Documents
- Spec: `docs/specs/TASK-001.md`
- Architecture: `ARCHITECTURE.md`
- API Contract: `API_SPEC.yaml`
- Test Scenarios: `docs/test-scenarios/TASK-006.md`
- Review Report: `docs/review-reports/TASK-007.md`
- Test Report: `docs/test-reports/TASK-009.md`
```

---

## 🏷️ Status Legend

| Symbol | Status | Meaning |
|---|---|---|
| 🔵 | BACKLOG | Not yet started, awaiting dependency |
| 🟡 | TODO | Ready to start, dependency cleared |
| 🔄 | IN_PROGRESS | Agent actively working |
| 🔍 | IN_REVIEW | Submitted to @Code-Review |
| 🧪 | IN_QA | Submitted to @QA-Tester |
| ✅ | DONE | Completed and verified |
| ❌ | BLOCKED | Waiting on external blocker |
| 🚫 | CANCELLED | Deprioritized or dropped |
