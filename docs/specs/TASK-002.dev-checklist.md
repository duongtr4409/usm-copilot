# Developer Checklist — TASK-002 (BE / FE / QA)

Date: 2026-04-22
Source: `docs/specs/TASK-002.md` + `docs/specs/TASK-002.addendum.md`
Purpose: concise, developer-facing tasks derived from the addendum. Use this as the single quick-reference for implementers and reviewers.

---

## Affected tasks
- Backend: TASK-005 (Backend Implementation)
- Frontend: TASK-006 (Frontend Implementation)
- QA: TASK-007 / TASK-009 (Test Preparation & Execution)

---

## Backend checklist (Java-BE)

1) API Endpoints / Contracts
- Implement/verify lazy-loading endpoints:
  - `GET /api/v1/org-units?parentId={id}&pageSize={n}` → returns children for parent, include `hasChildren` boolean.
  - (Optional) `GET /api/v1/org-units/{id}/subtree?depth={n}` for admin export.
- Ensure `POST /api/v1/classes/{classId}/students`:
  - Runs inside one DB transaction.
  - Validates username uniqueness and returns `409` on conflict.
  - On success returns: `{ studentProfileId, username, tempPassword }` to the caller (ADMIN) — do NOT log password.
  - Writes an `outbox` row for EnrollmentCreated within same tx.
- Conform to HTTP mapping: 409 (conflict), 422 (validation), 400 (bad request), 401/403, 423 (locked).

2) DB / Migrations
- Add migration (Flyway): `V10__staff_unit_transfer_audit.sql` (or similar) to create `staff_unit_transfer_audit` table:
  - Columns: `id`, `staff_id`, `previous_unit_id`, `new_unit_id`, `changed_by`, `changed_at`
- Verify `Staff.unit_id` validation (app layer). If enforcing at DB level, plan migration with triggers/checks and include backward-compatibility steps.
- Ensure `OrganizationUnit.path`/`ltree` and indexes exist for subtree queries; add indexes if missing.

3) Business rules & Validation
- Enforce: `Staff.unit_id` cannot reference OrganizationUnit.type = 'Lớp' (app-layer + DB safeguard if possible).
- When moving staff unit, create an audit row in `staff_unit_transfer_audit`.
- Add hasChildren flag calculation for org-units queries (efficient query or count > 0 check).

4) Tests (BE)
- Unit tests: `ClassService.addStudentToClass()` happy path + simulated failures to assert rollback.
- Integration tests (Testcontainers):
  - AddStudent happy path: assert user_account + student_profile + enrollment + outbox exist.
  - Rollback on failure: force a DB constraint and assert no partial records.
  - Username conflict returns 409 and no side-effects.
  - Org tree lazy-load behavior: parent fetch → children fetch.
  - Transfer audit test: change staff.unit_id → assert audit row created.

5) Operational / Security
- Do NOT log plaintext passwords. If returning tempPassword to ADMIN, return via secure response only; advise UI to prompt change on first login.
- Outbox write must be in same transaction as domain writes.
- Add metrics / logs for failures in enrollment flow.

---

## Frontend checklist (React-FE)

1) UI library
- Confirm library choice (Ant Design recommended). If FE chooses Tailwind/shadcn, provide mapping for Tree, Modal, Form components.

2) Organization Tree
- Implement Tree component with lazy-loading:
  - Initial load: `GET /api/v1/org-units?parentId=null&pageSize=...` (or existing root endpoint).
  - On expand: call `GET /api/v1/org-units?parentId={id}&pageSize={n}`.
  - Use `hasChildren` to show expand affordance.
  - Show loading spinner while fetching children and handle errors gracefully.

3) AddStudentToClass modal
- Fields: `Username` (required), `Initial Password` (required / or auto-generate), `Full Name`, `DOB`, `Contact`.
- Client-side validation matching server rules (min password length, username format).
- On success: display returned credentials in a secure modal with `Copy` action and warning to change password.
- Map API errors to UI states (409 → show duplicate username message; 422 → field validation errors).

4) Error / Permission handling
- Respect HTTP codes mapping. Show clear messages for 403/401.
- For account-lock (423) show appropriate guidance.

5) Tests (FE)
- E2E: Tree lazy-load flows, AddStudent form happy path, duplicate username rejection, UI display of returned credentials (copy button).
- Accessibility: ensure Tree and forms are keyboard-navigable and ARIA attributes present.

---

## QA checklist (QA-Tester)

1) Test scenarios to add
- TC-ORG-001: Lazy-loading tree — expand multiple levels, validate children count and hasChildren flags.
- TC-ADD-001: AddStudent happy path — verify account/profile/enrollment/outbox exist (integration).
- TC-ADD-002: AddStudent rollback — simulate failure after account creation, ensure rollback.
- TC-ADD-003: Username uniqueness — attempt duplicate username → expect 409 and no side-effects.
- TC-STAFF-TRANSFER-001: Staff unit change creates audit row with correct previous/new IDs and changed_by.
- TC-SEC-001: Ensure temp passwords do not appear in server logs or test output.
- TC-API-ERR-001: Verify API returns documented status codes for errors.

2) Test environment
- Use Testcontainers for integration: Postgres (matches migration), Redis (refresh tokens/outbox worker), backend image built locally.
- Define test data fixtures for deep org trees to exercise lazy-loading performance.

3) Non-functional tests
- Performance: measure expand latency for tree nodes with many children.
- Security: confirm password storage hashed; check no temp password leak in logs.

---

## Acceptance mapping (quick)
- UI AddStudent form fields ↔ US-010 (AC-10.2)
- AddStudent atomic flow ↔ US-005 (AC-5.1..5.3)
- Staff transfer audit ↔ (new addendum AC)
- Lazy-loading tree ↔ implied NFR: tree must support large N (addendum clarification)

---

## Quick next steps
1. BE: review checklist and confirm DB migration scope (audit table). If migration needed, respond here so PMO can schedule.
2. FE: confirm UI library choice and API params for lazy-loading.
3. QA: add the listed test cases to `docs/test-scenarios/` and schedule integration runs.

---

*End of checklist.*