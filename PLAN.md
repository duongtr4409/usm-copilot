# PLAN: Academy Management System (AMS) — Initial Roadmap

Project: Academy Management System (AMS)

Source requirement: [inbox/wUser/myRequirement.md](inbox/wUser/myRequirement.md)

Overview:
- Deliver an Admin Portal and Student Portal implementing organization tree management, staff & teacher management, RBAC, class enrollment with automatic student account creation, and news/training features as specified in the source requirements.

Task mapping (PMO canonical tasks):

| Task ID | Title | Owner | Expected Deliverable |
|---|---:|---|---|
| TASK-001 | Create PLAN & TASK IDs | @PMO | This PLAN.md and TASK_BOARD entry |
| TASK-002 | Business Requirements Analysis | @BA | `docs/specs/TASK-002.md`: User Stories + Acceptance Criteria (English)
| TASK-003 | System Architecture Design | @Tech-lead | `ARCHITECTURE.md`, `API_SPEC.yaml` updates
| TASK-004 | Database Schema Design | @DB-Admin | SQL migration(s) and `docs/db/schema-TASK-004.md`
| TASK-005 | Backend Implementation | @Java-BE | Backend service implementing API_SPEC.yaml, unit tests
| TASK-006 | Frontend Implementation | @React-FE | React UI, Tree-view, forms, integration with backend
| TASK-007 | QA Test Preparation | @QA-Tester | `docs/test-scenarios/TASK-007.md` (test cases)
| TASK-008 | Code Review | @Code-Review | Review reports in `docs/review-reports/TASK-008.md` and LGTM
| TASK-009 | QA Test Execution | @QA-Tester | Test reports in `docs/test-reports/TASK-009.md` and ALL_TESTS_PASSED
| TASK-010 | Implement AddStudentToClass transactional workflow | @Java-BE | Backend service implementing atomic CreateAccount + CreateProfile + Enrollment + outbox; integration tests
| TASK-011 | DevOps: Docker & Compose deployment | @DevOps-Engine | Dockerfiles for `backend` and `frontend`, `docker-compose.yml`, `.env.example`, and `docs/deploy/Docker-Compose.md`
| TASK-016 | Unblock integration tests | @DevOps-Engine / @Java-BE | Resolve Testcontainers ↔ Docker API negotiation or implement reliable compose-based integration test path; fix Flyway seed ordering; run full integration tests and produce test report and PR. |

Priority: HIGH

Acceptance criteria (project-level):
- Organization units editable via tree-view with unlimited depth.
- Staff assigned according to business rules (one admin unit per staff; class assignments separate).
- Adding a student to a class performs a transactional CreateProfile + CreateAccount + Enrollment.
- RBAC with ADMIN and STUDENT roles enforced.
- Frontend and backend integrated; automated tests and CI in place.

Next actions (immediate):
1. Hand off requirement doc to @BA to produce User Stories and Acceptance Criteria for TASK-002.
2. Create `docs/specs/TASK-002.md` from BA output.
3. After BA completes, trigger TASK-003 (Tech-lead architecture design).

PMO hand-off (to be sent to @BA):
@BA: "Analyze the following requirements and write User Stories with Acceptance Criteria for TASK-002: Academy Management System (AMS)."
Context:
  - Task ID: TASK-002
  - Input files: [inbox/wUser/myRequirement.md](inbox/wUser/myRequirement.md)
  - Expected output: `docs/specs/TASK-002.md` containing detailed User Stories and Acceptance Criteria in English.
  - Approval required from: @PMO

Timestamp: 2026-04-21

PMO hand-off (to be sent to @Java-BE):
@Java-BE: "Implement TASK-010: AddStudentToClass transactional backend workflow."
Context:
  - Task ID: TASK-010
  - Input files: [docs/specs/TASK-002.md](docs/specs/TASK-002.md), [API_SPEC.yaml](API_SPEC.yaml), db/migrations/
  - Expected output: Implementation of `POST /api/v1/classes/{classId}/students` with controller, service (`addStudentToClass`), repository implementations, outbox write, and integration tests (Testcontainers) validating TC-ADD-001..TC-ADD-003.
  - Approval required from: @Code-Review

Timestamp: 2026-04-21T08:31:00Z

PMO hand-off (to be sent to @DevOps-Engine):
@DevOps-Engine: "Create Dockerfiles and a docker-compose deployment for AMS."
Context:
  - Task ID: TASK-011
  - Input files: backend/ (pom.xml, src, db/migrations), frontend/ (package.json, src), db/migrations/
  - Expected output: `backend/Dockerfile`, `frontend/Dockerfile`, `docker-compose.yml` at repo root, `.env.example`, `docs/deploy/Docker-Compose.md`. Ensure the compose file includes Postgres (v15), Redis (optional), and services for backend & frontend; provide dev override `docker-compose.override.yml` and healthchecks. If Flyway is not present in `backend/pom.xml`, add Flyway dependency or add a simple migration init step to apply `db/migrations`.
  - Approval required from: @PMO and @Code-Review

Timestamp: 2026-04-21T08:55:00Z

PMO hand-off (to be sent to @DevOps-Engine):
@DevOps-Engine: "TASK-016: Diagnose and remediate Testcontainers/Docker API mismatch preventing integration tests from running reliably. Iterate docker-java and Testcontainers versions (only those resolvable from Maven Central), attempt CLI provider strategy, and if unresolved implement a compose-based test path: ensure Postgres init runs Flyway migrations in the correct order and provide wrapper scripts to run integration tests against compose DB. Produce reproducible steps, updated scripts, updated `backend/pom.xml` or dependencyManagement if required, and logs/artifacts of full integration test runs."
Context:
  - Task ID: TASK-016
  - Input files: `backend/pom.xml`, `scripts/`, `db/migrations/`, `docker-compose.yml`, `backend/src/test/java/com/usm/ams/integration/AddStudentToClassIntegrationTest.java`, `backend/src/test/resources/db/migration/V11__seed_test_user.sql`
  - Expected output: Wrapper scripts, updated `pom.xml` or dependency entries (if needed), test logs, runbook for reproducing the fix, and a branch/PR if changes were made
  - Approval required from: @PMO and @Java-BE

PMO hand-off (to be sent to @Java-BE):
@Java-BE: "TASK-016: Investigate and fix Flyway migration/seed ordering and test configuration causing `V11__seed_test_user.sql` failures when running integration tests against compose DB. Make seeds idempotent (e.g. use `ON CONFLICT DO NOTHING`), adjust migration numbering or test setup so migrations run before seeds, and ensure tests wait for DB readiness. Run integration tests against the compose DB and report results."
Context:
  - Task ID: TASK-016
  - Input files: `db/migrations/`, `backend/src/test/resources/db/migration/V11__seed_test_user.sql`, `backend/src/test/java/com/usm/ams/integration/AddStudentToClassIntegrationTest.java`
  - Expected output: Updated migrations/seeds, updated test configuration, passing integration test run logs, and a branch/PR with changes
  - Approval required from: @PMO and @DevOps-Engine

Timestamp: 2026-04-22T08:50:00Z