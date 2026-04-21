# Backend — Integration Tests

This document explains how to run backend integration tests (Testcontainers) locally
when your Docker server and the docker-java/Testcontainers client report an API
version mismatch.

Quick: use the provided wrapper script from the repo root:

```bash
./scripts/run-integration-tests.sh
```

What the script does
- Ensures `DOCKER_API_VERSION` is set (default `1.40`) so Testcontainers' docker-java
  client talks a compatible API with the local Docker server.
- Runs the `AddStudentToClassIntegrationTest` integration test via Maven.

Override the API version (if you have a newer server):

```bash
DOCKER_API_VERSION=1.41 ./scripts/run-integration-tests.sh
```

If the wrapper does not resolve failures, follow the long-term remediation in
`docs/deploy/Docker-Compose.md` (upgrade Docker Engine or pin Testcontainers/docker-java).
