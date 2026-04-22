TASK-021: Fix Outbox payload type mismatch

- Change: Modified `db/migrations/V8__outbox.sql` to set `payload TEXT NOT NULL` (previously `JSONB`).
- Reason: The `Outbox` JPA entity defines `payload` as `text` which caused SQL insertion errors when migrations created a `JSONB` column. For the test environment the simplest remedy is to align the migration with the entity.
- Action taken: Updated migration, created branch `TASK-021/fix-outbox-payload-type`, and re-ran integration tests.
- Tests executed: `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest`
- Result: Integration tests passed (AddStudentToClassIntegrationTest — Tests run: 3, Failures: 0, Errors: 0).
- Logs: `logs/TASK-021/runner-output.log`, `logs/TASK-021/maven-output.log`, `logs/TASK-021/db-container.log`

Notes & Production recommendation:
- This change mutates an existing migration file (acceptable for local/test environments). For production you MUST NOT edit already-applied migrations. Instead create a new, non-destructive migration (e.g. `V12__convert_outbox_payload_to_text.sql`) that safely converts or casts the column type and preserves data, with appropriate backups and verification steps.
- Suggested production migration approach (safe):
  1. Add a new `payload_text` column `TEXT`.
  2. Populate it from `payload` using `UPDATE outbox SET payload_text = payload::text`.
  3. Verify integrity, then drop old column and rename `payload_text` to `payload` (or use `ALTER COLUMN TYPE` with `USING` clause after verification).

Approvers: @PMO, @Java-BE
