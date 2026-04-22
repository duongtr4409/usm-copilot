#!/usr/bin/env bash
set -uo pipefail

# Try a list of docker-java versions and run a single integration test until one succeeds.
# Usage: ./scripts/try-dockerjava-versions.sh

: "${TESTCONTAINERS_DOCKER_CLIENT_STRATEGY:=org.testcontainers.dockerclient.DockerCliClientProviderStrategy}"
DOCKER_API_VERSION="${DOCKER_API_VERSION:-$(docker version --format '{{.Server.APIVersion}}' 2>/dev/null || echo '')}"
if [ -z "$DOCKER_API_VERSION" ]; then
  echo "ERROR: Could not detect Docker Server API version. Export DOCKER_API_VERSION or ensure docker is available." >&2
  exit 2
fi

export TESTCONTAINERS_DOCKER_CLIENT_STRATEGY DOCKER_API_VERSION RUN_INTEGRATION_TESTS=true

echo "Using DOCKER_API_VERSION=$DOCKER_API_VERSION"

VERSIONS=(
  3.3.3
  3.3.4
  3.3.5
  3.3.6
  3.3.7
  3.3.8
  3.3.9
  3.3.10
  3.3.11
  3.3.12
  3.3.13
  3.3.14
)

for v in "${VERSIONS[@]}"; do
  echo
  echo "==== Testing docker-java $v ===="
  mvn -U -f backend/pom.xml -Ddocker.java.version="$v" -Dtestcontainers.version=1.20.3 -Dtest=AddStudentToClassIntegrationTest -DtrimStackTrace=false test || rc=$?
  rc=${rc:-$?}
  if [ "$rc" = "0" ]; then
    echo "SUCCESS: docker-java=$v"
    exit 0
  else
    echo "FAILED: docker-java=$v (exit=$rc)"
  fi
done

echo "All tried docker-java versions failed."
exit 1
