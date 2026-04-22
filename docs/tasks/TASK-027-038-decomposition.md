# TASK-027..TASK-038 — Decomposition of TASK-005 (Backend) and TASK-006 (Frontend)

Overview
--------
This document breaks TASK-005 (Backend Implementation) and TASK-006 (Frontend Implementation) into small, developer-friendly tasks that can be assigned, implemented, and reviewed independently. Each task includes owner, concise description, acceptance criteria, dependencies, suggested branch name, and estimated effort.

Goal
----
Enable `@Java-BE` and `@React-FE` to pick up small tasks, implement them, run unit tests, and open focused PRs which are easy to review and integrate. `@Tech-lead` should review and confirm estimates/ownership.

Tasks (summary)
---------------
- TASK-027 — Backend: `GET /api/v1/classes` (Owner: @Java-BE)
- TASK-028 — Backend: OrganizationUnit CRUD (`/org-units`) (Owner: @Java-BE)
- TASK-029 — Backend: Staff CRUD (`/staff`) (Owner: @Java-BE)
- TASK-030 — Backend: Student endpoints (`/api/v1/students`) (Owner: @Java-BE)
- TASK-031 — Backend: Class roster endpoint (`GET /api/v1/classes/{classId}/students`) (Owner: @Java-BE)
- TASK-032 — Backend: News endpoints (`/news`) (Owner: @Java-BE)
- TASK-033 — Frontend: ClassPicker integration (Owner: @React-FE)
- TASK-034 — Frontend: AddStudent form polish and integration (Owner: @React-FE)
- TASK-035 — Frontend: OrgUnit tree UI (Owner: @React-FE)
- TASK-036 — Frontend: Staff management UI (Owner: @React-FE)
- TASK-037 — Frontend: News management UI (Owner: @React-FE)
- TASK-038 — Frontend: Class roster UI (Owner: @React-FE)

Per-task details
----------------

### TASK-027 — Backend: `GET /api/v1/classes`
- Owner: @Java-BE
- Branch: `TASK-027/backend-get-classes`
- Description: Implement `GET /api/v1/classes` returning an array of organization units filtered to type `Lớp` (class). Add repository method `OrganizationUnitRepository.findByType(String type)` or equivalent. Add DTO mapping and unit tests.
- Acceptance criteria:
  - Endpoint returns 200 with an array of class objects: {id, code, title}.
  - Unit tests cover repository query and controller mapping.
  - Frontend `ClassPicker` can consume the payload without changes.
- Dependencies: `db/migrations/V3__organization_units.sql` present.
- Estimated effort: 1-2 days.

### TASK-028 — Backend: OrganizationUnit CRUD
- Owner: @Java-BE
- Branch: `TASK-028/backend-orgunit-crud`
- Description: Implement `/org-units` CRUD endpoints (POST /org-units, GET /org-units, GET /org-units/{id}, PUT, DELETE). Enforce validation and ensure reparenting rules do not violate constraints. Add unit + integration tests.
- Acceptance criteria:
  - CRUD endpoints implemented and protected by ADMIN role (via `@PreAuthorize`).
  - Tests for creating, updating, deleting and listing tree nodes.
- Dependencies: TASK-027 recommended before UI work.
- Estimated effort: 3-5 days.

### TASK-029 — Backend: Staff CRUD
- Owner: @Java-BE
- Branch: `TASK-029/backend-staff-crud`
- Description: Implement `/staff` endpoints, link `Staff` to `UserAccount`, enforce business rules (one admin unit per staff), and include search/list pagination.
- Acceptance criteria: endpoints working and tested; staff creation creates linked account when requested.
- Estimated effort: 2-4 days.

### TASK-030 — Backend: Student endpoints
- Owner: @Java-BE
- Branch: `TASK-030/backend-students`
- Description: Implement `GET /api/v1/students/{id}`, `PUT /api/v1/students/{id}` to view and update student profile info (excluding sensitive auth fields).
- Acceptance criteria: endpoints and tests present; profile updates are validated and persisted.
- Estimated effort: 1-2 days.

### TASK-031 — Backend: Class roster endpoint
- Owner: @Java-BE
- Branch: `TASK-031/backend-class-roster`
- Description: Implement `GET /api/v1/classes/{classId}/students` returning enrolled student profiles for `classId`. Apply RBAC: ADMIN or class-scoped admin.
- Acceptance criteria: Endpoint returns roster; integration test demonstrates AddStudentToClass + roster returns added student.
- Estimated effort: 1-2 days.

### TASK-032 — Backend: News endpoints
- Owner: @Java-BE
- Branch: `TASK-032/backend-news`
- Description: Implement `/news` list/create/publish endpoints and ensure published events write to outbox.
- Acceptance criteria: create/publish flow works and outbox entry created; tests included.
- Estimated effort: 2-3 days.

### TASK-033 — Frontend: ClassPicker integration
- Owner: @React-FE
- Branch: `TASK-033/frontend-classpicker`
- Description: Use `GET /api/v1/classes` to populate `ClassPicker` and add loading/error states and tests.
- Acceptance criteria: `ClassPicker` shows classes from backend and tests mock/verify behavior.
- Estimated effort: 0.5-1 day.

### TASK-034 — Frontend: AddStudent form polish
- Owner: @React-FE
- Branch: `TASK-034/frontend-addstudent`
- Description: Improve UX for `AddStudentForm`, show server validation errors, disable submit while pending, and keep tests green when integrated with real backend.
- Acceptance criteria: UX improvements and test coverage; successful manual test with backend stub.
- Estimated effort: 1-2 days.

### TASK-035 — Frontend: OrgUnit tree UI
- Owner: @React-FE
- Branch: `TASK-035/frontend-orgtree`
- Description: Implement hierarchical tree view and CRUD flows for org-units; optimistic UI and error handling.
- Acceptance criteria: create/update/reparent/delete flows work with backend; tests included.
- Estimated effort: 3-5 days.

### TASK-036 — Frontend: Staff management UI
- Owner: @React-FE
- Branch: `TASK-036/frontend-staff`
- Description: Implement staff list/create/edit and assign to org-units.
- Acceptance criteria: UI flows working and tested; integrates with backend endpoints.
- Estimated effort: 2-4 days.

### TASK-037 — Frontend: News management UI
- Owner: @React-FE
- Branch: `TASK-037/frontend-news`
- Description: RTE-based create/edit/publish news management page; publish triggers backend outbox.
- Acceptance criteria: posts can be created, previewed, published; test coverage for critical interactions.
- Estimated effort: 2-4 days.

### TASK-038 — Frontend: Class roster UI
- Owner: @React-FE
- Branch: `TASK-038/frontend-roster`
- Description: Implement the class roster page, list students and allow basic actions (view profile, invite, remove).
- Acceptance criteria: roster displays enrolled students; actions wire to backend endpoints (stubs acceptable for first PR).
- Estimated effort: 1-3 days.

Handoffs / Next steps
---------------------
1. @Tech-lead: Review this decomposition, confirm owners, adjust estimates, and set priorities. Use branches named above.

2. @Java-BE: Please pick `TASK-027` first (GET /classes) to unblock frontend `ClassPicker`. After implementing, run unit tests and open PR `TASK-027/backend-get-classes` with reviewer `@Code-Review` and link this task file.

3. @React-FE: Once `TASK-027` PR is merged (or a mock backend contract is agreed), pick `TASK-033` to integrate `ClassPicker` and add tests. Open PR `TASK-033/frontend-classpicker`.

4. @PMO: After Tech-lead confirms, update `TASK_BOARD.md` statuses and assign owners; then PMO will create small task entries in tracking board and update `.github/copilot-agents/LOGS.json`.

Contact & tracking
------------------
- Use branch names above and include `TASK-0XX` in PR titles.
- Add short description and `RUN_INTEGRATION_TESTS=true` notes if integration test coverage included.
- Attach unit test logs under `logs/TASK-0XX/` for each task.

## Confirmed assignments (Tech-lead review)
- Date: 2026-04-22
- Reviewed by: @Tech-lead

Confirmed mapping (YAML):

```yaml
TASK-027: { owner: '@Java-BE', estimate_days: 2, priority: 'High', blockers: [] }
TASK-028: { owner: '@Java-BE', estimate_days: 5, priority: 'Medium', blockers: [] }
TASK-029: { owner: '@Java-BE', estimate_days: 4, priority: 'Medium', blockers: [] }
TASK-030: { owner: '@Java-BE', estimate_days: 2, priority: 'Medium', blockers: [] }
TASK-031: { owner: '@Java-BE', estimate_days: 2, priority: 'Medium', blockers: [] }
TASK-032: { owner: '@Java-BE', estimate_days: 3, priority: 'Low', blockers: [] }
TASK-033: { owner: '@React-FE', estimate_days: 1, priority: 'High', blockers: [ 'TASK-027' ] }
TASK-034: { owner: '@React-FE', estimate_days: 2, priority: 'Medium', blockers: [] }
TASK-035: { owner: '@React-FE', estimate_days: 5, priority: 'Medium', blockers: [] }
TASK-036: { owner: '@React-FE', estimate_days: 4, priority: 'Medium', blockers: [] }
TASK-037: { owner: '@React-FE', estimate_days: 4, priority: 'Low', blockers: [] }
TASK-038: { owner: '@React-FE', estimate_days: 3, priority: 'Medium', blockers: [] }
```

Re-sequencing & Blockers:

- TASK-027 is the top priority (start immediately) to unblock frontend TASK-033.
- Confirm RBAC model for class-scoped admin access (used by TASK-031); if ambiguous, Tech-lead will assume `ADMIN` or a `CLASS_ADMIN` staff role scoped to org-unit — escalate only if BA/PMO rejects this assumption.
- `OrgUnit` reparenting rules in TASK-028 may require BA confirmation for edge-cases (moving units with students/staff); plan to implement safe validation and reject destructive moves unless confirmed by BA.

No other architectural decisions are required now; proceed with implementation under these assumptions. Escalate to @PMO only if BA rejects RBAC or reparenting assumptions above.


