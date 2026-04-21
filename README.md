# 🤖 USM — Unified Software Machine
## Multi-Agent Orchestrator System

> A self-operating software development machine powered by a coordinated team of AI agents.

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        USER / STAKEHOLDER                        │
└───────────────────────────────┬─────────────────────────────────┘
                                │ Request
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                         @PMO (Orchestrator)                      │
│          Coordinates · Plans · Delegates · Reports              │
└──────┬──────────┬───────────────────────┬────────────┬──────────┘
       │          │                       │            │
       ▼          ▼                       ▼            ▼
   @BA         @Tech-lead           @DevOps-Engine  @DB-Admin
  (Analysis)   (Architecture)       (CI/CD)         (Schema)
       │          │
       └────┬─────┘
            │ API_SPEC.yaml + ARCHITECTURE.md
            ▼
  ┌─────────────────────┐
  │    Parallel Dev      │
  │  @Java-BE @React-FE │
  └────────┬────────────┘
           │ Code Artifacts
           ▼
      @Code-Review
           │ LGTM / Reject
           ▼
       @QA-Tester
           │ PASS / FAIL
           ▼
      @PMO → Reports "Ready to Merge"
```

---

## 👥 Agent Roster

| Agent | Role | Primary Outputs |
|---|---|---|
| `@PMO` | Orchestrator & Project Manager | PLAN.md, DASHBOARD.md, TASK_BOARD.md |
| `@BA` | Business Analyst | User Stories, Acceptance Criteria |
| `@Tech-lead` | Solutions Architect | ARCHITECTURE.md, API_SPEC.yaml |
| `@Java-BE` | Backend Engineer (Spring Boot) | Java source code, unit tests |
| `@React-FE` | Frontend Engineer (React) | React components, state management |
| `@Code-Review` | Code Quality Inspector | Review reports, LGTM approvals |
| `@QA-Tester` | Quality Assurance | Test scripts, test reports |
| `@DB-Admin` | Database Administrator | Schema migrations, SQL optimizations |
| `@DevOps-Engine` | Infrastructure Engineer | Dockerfile, CI/CD pipelines |

---

## 📁 Directory Structure

```
USM/
├── .github/
│   └── copilot-agents/
│       ├── PMO.agent.md           # PMO agent instructions
│       ├── BA.agent.md            # BA agent instructions
│       ├── Tech-lead.agent.md     # Tech-lead agent instructions
│       ├── Java-BE.agent.md       # Java Backend agent instructions
│       ├── React-FE.agent.md      # React Frontend agent instructions
│       ├── Code-Review.agent.md   # Code Review agent instructions
│       ├── QA-Tester.agent.md     # QA Tester agent instructions
│       ├── DB-Admin.agent.md      # DB Admin agent instructions
│       ├── DevOps-Engine.agent.md # DevOps agent instructions
│       ├── LOGS.json              # Transaction log for all handoffs
│       └── PLAN.md                # Active sprint plan
├── docs/
│   ├── specs/                     # BA-produced feature specifications
│   ├── api/                       # API contract files (YAML/OpenAPI)
│   └── test-reports/              # QA test execution reports
├── inbox/                         # Incoming tasks for each agent
├── outbox/                        # Completed artifacts from each agent
├── ARCHITECTURE.md                # System design (owned by @Tech-lead)
├── API_SPEC.yaml                  # API contracts (owned by @Java-BE)
├── CONVENTIONS.md                 # Coding standards (used by @Code-Review)
├── TASK_BOARD.md                  # Sprint board (managed by @PMO)
└── DASHBOARD.md                   # Live progress dashboard (by @PMO)
```

---

## 🚀 Quick Start — Invoking the System

1. **Open** `.github/copilot-agents/PMO.agent.md` in GitHub Copilot Chat.
2. **Type your feature request** naturally. Example:
   ```
   @PMO Build a user authentication module with JWT tokens,
   role-based access control, and a React login/register UI.
   ```
3. **Let PMO coordinate** — it will delegate to agents in the correct sequence.
4. **Track progress** in `DASHBOARD.md` and `TASK_BOARD.md`.

---

## 📋 Communication Protocol

### Hand-off Syntax
```
@{agent_name}: "{instruction}" [Context: {relevant files}]
```

### Approval Gates
| Gate | Required Approval | Condition to Pass |
|---|---|---|
| Design → Dev | `@PMO` | @Tech-lead completes ARCHITECTURE.md |
| Code → Review | `@Code-Review` | Automated trigger on COMPLETED status |
| Review → QA | `@Code-Review` | Returns `LGTM` |
| QA → Merge | `@PMO` | @QA-Tester returns `ALL_TESTS_PASSED` |

---

## 📜 Key Documents

| File | Owner | Purpose |
|---|---|---|
| [ARCHITECTURE.md](./ARCHITECTURE.md) | @Tech-lead | System design & DB diagrams |
| [API_SPEC.yaml](./API_SPEC.yaml) | @Java-BE | OpenAPI endpoint contracts |
| [CONVENTIONS.md](./CONVENTIONS.md) | @Code-Review | Coding standards & rules |
| [TASK_BOARD.md](./TASK_BOARD.md) | @PMO | Feature status tracking |
| [DASHBOARD.md](./DASHBOARD.md) | @PMO | Live agent progress dashboard |
| [LOGS.json](./.github/copilot-agents/LOGS.json) | All Agents | Transaction & handoff log |
