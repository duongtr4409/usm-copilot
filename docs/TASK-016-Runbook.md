**TASK-016: Testcontainers / Docker API mismatch — Runbook**

- **Problem:** Maven build failed resolving pinned `com.github.docker-java` artifacts (pom forced `3.4.15`) causing Testcontainers runs to fail before tests start. Also forked surefire JVMs did not automatically receive Testcontainers/Docker env overrides in some CI setups.

- **What I changed:**
  - Removed the explicit `docker-java` dependency overrides from [backend/pom.xml](backend/pom.xml) so Testcontainers' BOM manages the docker-java version.
  - Configured `maven-surefire-plugin` to propagate `DOCKER_API_VERSION`, `DOCKER_HOST`, `TESTCONTAINERS_DOCKER_CLIENT_STRATEGY` and compose-related env vars into forked test JVMs.
  - Added a compose-based integration-test wrapper script: `scripts/run-integration-tests-compose.sh` which composes a temporary DB init directory combining repo migrations + test seeds, starts `db` + `redis`, waits for readiness, then runs the requested integration test with `RUN_INTEGRATION_TESTS=true` and `USE_COMPOSE_DB=true`.

- **How to reproduce (Testcontainers path):**
  1. Ensure Docker is running and reachable from the shell.
 2. From repo root run:
```
./scripts/try-testcontainers.sh
```
  This will attempt multiple Testcontainers versions; after the POM fix it should reach the Testcontainers startup step instead of failing dependency resolution.

- **How to run the Compose path (recommended when Testcontainers cannot connect to Docker):**
  1. Run the wrapper (will rebuild a fresh DB using migrations and test seed):
```
./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest
```
  2. To run the full integration suite, pass a different `-Dtest` or omit to run defaults. The script exports `RUN_INTEGRATION_TESTS=true` and `USE_COMPOSE_DB=true` so tests will use the compose DB.

- **Why this fixes the issue:**
  - Removing the forced docker-java override eliminates attempts to download a non-existent artifact (3.4.15) from Maven Central; Testcontainers specifies a compatible docker-java version via its BOM.
  - Propagating env vars into forked JVMs ensures `TESTCONTAINERS_DOCKER_CLIENT_STRATEGY` and `DOCKER_API_VERSION` are visible to tests started by surefire/failsafe.
  - Compose-based path avoids Testcontainers/docker-client altogether and is useful for local CI debugging and reproducible test runs.

- **Next steps / PR:**
  - Review changes in `backend/pom.xml` and `scripts/run-integration-tests-compose.sh`.
  - If approved, push branch `TASK-016/fix-testcontainers-docker-java` and open PR requesting approvals from @PMO and @Java-BE.

  CI Integration (GitHub Actions)
  ------------------------------

  This repository includes a lightweight GitHub Actions workflow to run unit tests and the compose-backed integration test wrapper on pull requests to the `main` branch.

  - Workflow file: `.github/workflows/compose-integration.yml`
  - It runs unit tests using Maven, then brings up `db` and `redis` via `docker compose` and executes the script `./scripts/run-integration-tests-compose.sh AddStudentToClassIntegrationTest`.

  Notes:
  - Runners must have Docker and `docker compose` available (ubuntu-latest typically supports Docker). If your organization uses self-hosted runners, ensure Docker is installed and the runner has sufficient privileges.
  - For security reasons the workflow does not persist secrets; if the compose files require custom passwords, use repository secrets and an override compose file in the PR.


**Notes**
- The repository already contains helpful scripts `scripts/try-dockerjava-versions.sh` and `scripts/try-testcontainers.sh` — use them to iterate versions if further tuning is required.
