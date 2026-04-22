#!/usr/bin/env bash
set -euo pipefail

# Run integration tests against a Docker Compose Postgres (no Testcontainers).
# Creates a temporary init folder combining project migrations and test seeds,
# starts db+redis, waits for readiness, then runs the specified test (or default).

: "${1:-}" >/dev/null 2>&1 || true

POSTGRES_USER=${POSTGRES_USER:-usm}
POSTGRES_PASSWORD=${POSTGRES_PASSWORD:-changeme}
POSTGRES_DB=${POSTGRES_DB:-usm}
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

TEST=${1:-AddStudentToClassIntegrationTest}

export RUN_INTEGRATION_TESTS=true
export USE_COMPOSE_DB=true
export COMPOSE_DB_URL=${COMPOSE_DB_URL:-"jdbc:postgresql://localhost:5432/${POSTGRES_DB}"}
export COMPOSE_DB_USERNAME=${COMPOSE_DB_USERNAME:-$POSTGRES_USER}
export COMPOSE_DB_PASSWORD=${COMPOSE_DB_PASSWORD:-$POSTGRES_PASSWORD}

TMP_INIT_DIR=$(mktemp -d)
trap 'rm -rf "$TMP_INIT_DIR"; rm -f docker-compose.test.override.yml' EXIT

echo "Preparing DB init directory: $TMP_INIT_DIR"
cp -a "$ROOT_DIR/db/migrations/." "$TMP_INIT_DIR/" || true

TEST_SEED="$ROOT_DIR/backend/src/test/resources/db/migration/V11__seed_test_user.sql"
if [ -f "$TEST_SEED" ]; then
  echo "Including test seed: $TEST_SEED"
  cp "$TEST_SEED" "$TMP_INIT_DIR/"
fi

cat > docker-compose.test.override.yml <<EOF
version: '3.8'
services:
  db:
    volumes:
      - ${TMP_INIT_DIR}:/docker-entrypoint-initdb.d:ro
EOF

# Determine compose command: prefer `docker-compose` binary, fallback to `docker compose` plugin
if command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_CMD=(docker-compose)
elif command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD=(docker compose)
else
  echo "Error: neither 'docker-compose' nor 'docker compose' is available in PATH" >&2
  exit 1
fi

echo "Tearing down any existing compose stack and volumes..."
"${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml down -v || true

echo "Starting db and redis..."
"${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml up -d db redis

echo "Waiting for Postgres to be ready..."
until "${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml exec -T db pg_isready -U "${POSTGRES_USER}" >/dev/null 2>&1; do
  sleep 1
done
echo "Postgres is ready."

echo "Waiting for DB initialization (migrations + seeds) to finish..."
attempt=0
max_attempts=120
until "${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml exec -T db \
  psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" -tAc "SELECT 1 FROM information_schema.tables WHERE table_schema='public' AND table_name='roles';" | grep -q 1; do
  attempt=$((attempt+1))
  if [ $attempt -ge $max_attempts ]; then
    echo "Timed out waiting for DB initialization (roles table)." >&2
    "${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml logs db || true
    exit 1
  fi
  sleep 1
done
echo "DB initialization complete."

echo "Running Maven integration test: $TEST"
exec mvn -f "$ROOT_DIR/backend/pom.xml" -DskipTests=false -Dtest="$TEST" test
