#!/usr/bin/env bash
set -euo pipefail

# Try multiple Testcontainers versions for the AddStudent integration test.
# Usage: DOCKER_API_VERSION=1.54 ./scripts/try-testcontainers.sh

DOCKER_API_VERSION=${DOCKER_API_VERSION:-"$(docker version --format '{{.Server.APIVersion}}' 2>/dev/null || echo '')"}
if [ -z "$DOCKER_API_VERSION" ]; then
  echo "Could not detect Docker Server API version. Please set DOCKER_API_VERSION env var and ensure docker is available."
  exit 2
fi

export DOCKER_API_VERSION
export RUN_INTEGRATION_TESTS=true

VERSIONS=(
  1.20.3
  1.20.2
  1.20.1
  1.19.0
  1.18.3
  1.21.0
)

for v in "${VERSIONS[@]}"; do
  echo
  echo "==== Testing Testcontainers $v ===="
  mvn -U -f backend/pom.xml -Dtestcontainers.version="$v" -Dtest=AddStudentToClassIntegrationTest test || echo "FAILED for $v"
done

echo "Done. If none succeeded, consider running with a different set of versions or sharing docker/Testcontainers logs."
