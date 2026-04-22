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
| TASK-017 | Fix compose test credential propagation | @Java-BE / @DevOps-Engine | Patch `scripts/run-integration-tests-compose.sh` to pass explicit JVM system properties `-Dspring.datasource.url`, `-Dspring.datasource.username`, and `-Dspring.datasource.password` using detected container credentials; ensure these are forwarded to Maven/Surefire forks; run the specified integration test(s) and provide logs showing success or detailed failure. |
| TASK-018 | Fix enrollment table/entity naming mismatch | @Java-BE | Align JPA entity/table naming so application matches DB migrations (e.g., map `Enrollment` entity to `enrollments` table), re-run integration tests, and provide logs. |
| TASK-019 | Test profile: relax security for integration tests | @Java-BE | Add an `integration-tests`-only security configuration that permits test requests (e.g., disable auth or permit `/api/v1/**`) so compose-backed integration tests can exercise endpoints without JWT; document the change and run integration tests. |
| TASK-020 | Align enrollments FK to organization_unit | @DB-Admin | Update `db/migrations/V6__classes_enrollments.sql` so the enrollments table references `organization_unit` (use `class_unit_id` column) instead of `classes(id)`, to match application usage; re-run integration tests and provide logs. |
| TASK-021 | Align Outbox payload column type | @DB-Admin | Adjust `db/migrations/V8__outbox.sql` to use `payload TEXT` (or add a non-destructive V12 migration to convert payload to text) so it matches the JPA `Outbox.payload` mapping, re-run integration tests and provide logs. |

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

PMO hand-off (to be sent to @Java-BE):
@Java-BE: "TASK-017: Fix compose-runner credential propagation and re-run integration test."
Context:
  - Task ID: TASK-017
  - Input files: `scripts/run-integration-tests-compose.sh`, `backend/pom.xml`, `db/migrations/`, `backend/src/test/resources/db/migration/V11__seed_test_user.sql`
  - Expected output: Patch to `scripts/run-integration-tests-compose.sh` that passes explicit JVM properties to Maven (`-Dspring.datasource.url`, `-Dspring.datasource.username`, `-Dspring.datasource.password`) using detected container credentials; a successful run of `mvn -Dtest=AddStudentToClassIntegrationTest test` with logs showing Flyway and tests passing, or a detailed failure log and remediation steps. Commit changes to a branch and include test logs.
  - Approval required from: @PMO and @DevOps-Engine

PMO hand-off (to be sent to @Java-BE):
@Java-BE: "TASK-018: Fix DB table/entity naming mismatch for Enrollment and re-run integration tests."
Context:
  - Task ID: TASK-018
  - Input files: `backend/src/main/java/com/usm/ams/entity/Enrollment.java`, `db/migrations/V6__classes_enrollments.sql`, `backend/src/test/java/com/usm/ams/integration/AddStudentToClassIntegrationTest.java`
  - Expected output: Update `Enrollment` entity mapping to match the migration table name (map to `enrollments`), run `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest` to verify migrations + application schema align and that tests pass; capture logs under `logs/TASK-018/`; commit changes to branch `TASK-018/fix-enrollment-table-name` and include test logs and brief remediation notes.
  - Approval required from: @PMO and @Code-Review

PMO hand-off (to be sent to @Java-BE):
@Java-BE: "TASK-019: Add an integration-tests-only security configuration to permit test requests."
Context:
  - Task ID: TASK-019
  - Input files: `backend/src/main/java/com/usm/ams/security/` (existing), `backend/src/test/java/com/usm/ams/integration/AddStudentToClassIntegrationTest.java`, `scripts/run-integration-tests-compose.sh`
  - Expected output: Add a test-only security configuration (e.g., `TestSecurityConfig` annotated with `@Profile("integration-tests")`) that disables authentication or permits the needed endpoints; run `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest` and capture logs under `logs/TASK-019/`; commit changes to branch `TASK-019/disable-security-for-tests` with a short report in `outbox/TASK-019-report.md`.
  - Approval required from: @PMO and @Code-Review

PMO hand-off (to be sent to @DB-Admin):
@DB-Admin: "TASK-020: Update enrollments migration to reference organization_unit and align column naming."
Context:
  - Task ID: TASK-020
  - Input files: `db/migrations/V6__classes_enrollments.sql`, `backend/src/main/java/com/usm/ams/entity/Enrollment.java`, `backend/src/test/java/com/usm/ams/integration/AddStudentToClassIntegrationTest.java`
  - Expected output: Modify `V6__classes_enrollments.sql` so the enrollments table uses column `class_unit_id` (or ensure application mapping matches) and the foreign key `fk_enrollments_class` references `organization_unit(id)` (ON DELETE CASCADE). Ensure the migration remains idempotent. Re-run `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest` and capture logs under `logs/TASK-020/`. Commit changes to branch `TASK-020/fix-enrollments-fk` with a migration note.
  - Approval required from: @PMO and @Java-BE

PMO hand-off (to be sent to @DB-Admin):
@DB-Admin: "TASK-021: Fix Outbox payload column type mismatch for integration tests."
Context:
  - Task ID: TASK-021
  - Input files: `db/migrations/V8__outbox.sql`, `backend/src/main/java/com/usm/ams/entity/Outbox.java`, `scripts/run-integration-tests-compose.sh`
  - Expected output: Update `V8__outbox.sql` to change `payload JSONB` to `payload TEXT` (or add a safe V12 migration that adds `payload_text` column, copies JSON->text, and drops JSONB), re-run `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest` and capture logs under `logs/TASK-021/`; commit changes to branch `TASK-021/fix-outbox-payload-type` and include a short `outbox/TASK-021-report.md` explaining the choice.
  - Approval required from: @PMO and @Java-BE

Timestamp: 2026-04-22T08:50:00Z