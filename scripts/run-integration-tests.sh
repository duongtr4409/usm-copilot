#!/usr/bin/env bash
set -euo pipefail

# Wrapper to run the backend integration test(s) while setting a compatible
# Docker API version for Testcontainers/docker-java.
#
# By default this sets DOCKER_API_VERSION to 1.40 if it is not already set.
# To override use: DOCKER_API_VERSION=1.41 ./scripts/run-integration-tests.sh

export DOCKER_API_VERSION="${DOCKER_API_VERSION:-1.40}"
echo "Using DOCKER_API_VERSION=${DOCKER_API_VERSION}"

# Run the single integration test class. Additional maven args can be passed through.
mvn -f backend/pom.xml -Dtest=AddStudentToClassIntegrationTest test "$@"
