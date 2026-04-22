TASK-020: Fix enrollments foreign key (summary)

- **Branch:** TASK-020/fix-enrollments-fk
- **Commit:** cbafad1
- **Files changed:**
  - db/migrations/V6__classes_enrollments.sql — replaced `class_id` with `class_unit_id` and changed FK to reference `organization_unit(id)`. Updated unique constraint and index names. Kept `classes` table and triggers intact.
  - backend/src/main/java/com/usm/ams/entity/Enrollment.java — updated `@Column` to use `class_unit_id` to match migration.

- **Reason:** The application uses `OrganizationUnit` IDs for class references. The previous migration created a FK to `classes(id)`, which caused referential integrity failures during test runs when `OrganizationUnit` IDs were used directly.

- **Risk & choice:** I modified the existing V6 migration (minimal change requested) to make tests pass. Safer alternative: add a new migration (e.g., `V13__add_class_unit_id_to_enrollments.sql`) to add `class_unit_id`, backfill data, and add the FK. If you prefer the non-destructive approach I can create that instead.

- **Test run:** Executed `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest`.
  - Result: Build **FAILED**. Maven reported 1 failing test.
  - Failure reason: SQL type error when writing to `outbox.payload`: "column \"payload\" is of type jsonb but expression is of type character varying". This indicates the Outbox insertion needs to supply a JSONB value (e.g., use `to_jsonb(...)` or set the column binding to JSONB) — unrelated to the enrollments FK change.

- **Logs (in repository):**
  - logs/TASK-020/runner-output.log — full run output
  - logs/TASK-020/maven-output.log — Maven/test output (extracted)
  - logs/TASK-020/db-container.log — attempt to capture DB container logs; empty in this environment because `docker-compose` client is not available for log extraction

- **Next steps (recommendations):**
  1. Fix Outbox payload insertion to use JSONB values (quick fix for failing test).
  2. If you prefer not to edit historical migrations, let me know and I'll add a non-destructive migration to add `class_unit_id` and migrate data.

Please review and advise whether to proceed with the Outbox JSONB fix or to instead implement a non-destructive migration.

— @DB-Admin
