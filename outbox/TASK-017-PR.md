Branch: TASK-017/fix-compose-credentials
Commit: a07ff4634c74265ac0d4fb17093827c869393076

Summary:
- Pass compose-detected DB credentials to Maven and ensure forked test JVMs inherit them.
- Export `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.
- Also pass `-Dspring.datasource.*` system properties to Maven invocation.

Why:
- Integration tests were failing at Flyway startup due to the test JVM using different DB credentials.

How to run locally (same as script):

    ./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest

Changes:
- Modified: scripts/run-integration-tests-compose.sh

Notes:
- This change is limited to the local test runner; no application or production configuration changed.
- If the failure persists, the next step is to add a test-specific compose override to pin Postgres password to a known value (docker-compose.override.task-017.yml).

Logs produced (workspace):
- logs/TASK-017/runner-output.log
- logs/TASK-017/maven-output.log
- logs/TASK-017/db-container.log

Approvers required: @PMO, @DevOps-Engine
