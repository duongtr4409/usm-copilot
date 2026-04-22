# TASK-019 Report

- **Branch:** TASK-019/disable-security-for-tests
- **What I changed:**
  - Added test-only security config: `backend/src/main/java/com/usm/ams/config/TestSecurityConfig.java` (@Profile("integration-tests")).
  - Marked main security config inactive during integration tests: `backend/src/main/java/com/usm/ams/security/SecurityConfig.java` (@Profile("!integration-tests")).
  - Test config permits all requests and injects an ADMIN authentication for integration-tests, and provides a `PasswordEncoder` bean.

- **Commands run:**
  - `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest`

- **Logs produced:**
  - `logs/TASK-019/runner-output.log`
  - `logs/TASK-019/maven-output.log`
  - `logs/TASK-019/db-container.log`

- **Result:** Integration tests executed. The suite did not fully pass:
  - 1 failing test: `TC_ADD_001_happyPath_createsAccountProfileEnrollmentAndOutbox` (HTTP 500 due to DB foreign-key violation when inserting enrollment: class_id not present in "classes").

- **Notes / Next steps:**
  - The security changes correctly allow test requests (no HTTP 403). Remaining failures are due to DB schema/entity mapping alignment (migration vs entity table names for classes/enrollments).
  - Suggested follow-ups: align `OrganizationUnit`/`Enrollment` entity mappings with DB migrations or update migrations to create the expected `classes` table; or adjust tests to use seeded class id.

Please review and advise if you want me to (a) map entities to match migrations, (b) change migrations, or (c) adjust tests/seeds.
