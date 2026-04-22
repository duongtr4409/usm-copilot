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

# Normalize migration filenames so they sort numerically (V1,V2,V10 -> 001_V1...,002_V2...,010_V10...)
echo "Normalizing migration filenames for correct order..."
for src in "$TMP_INIT_DIR"/V*; do
  if [ -f "$src" ]; then
    base=$(basename "$src")
    ver=$(echo "$base" | sed -nE 's/^V([0-9]+)__.*$/\1/p')
    if [ -n "$ver" ]; then
      printf -v pad "%03d" "$ver"
      dest="$TMP_INIT_DIR/${pad}_$base"
      mv "$src" "$dest"
    fi
  fi
done

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
# Detect effective DB user and DB name inside the running container and use them for checks
echo "Detecting DB environment inside container..."
CONTAINER_PG_USER=$("${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml exec -T db env | awk -F= '/^POSTGRES_USER=/{print $2}' || true)
CONTAINER_PG_DB=$("${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml exec -T db env | awk -F= '/^POSTGRES_DB=/{print $2}' || true)
CONTAINER_PG_PASS=$("${COMPOSE_CMD[@]}" -f docker-compose.yml -f docker-compose.test.override.yml exec -T db env | awk -F= '/^POSTGRES_PASSWORD=/{print $2}' || true)
if [ -n "$CONTAINER_PG_USER" ]; then
  echo "Using container POSTGRES_USER=$CONTAINER_PG_USER"
  POSTGRES_USER="$CONTAINER_PG_USER"
fi
if [ -n "$CONTAINER_PG_DB" ]; then
  echo "Using container POSTGRES_DB=$CONTAINER_PG_DB"
  POSTGRES_DB="$CONTAINER_PG_DB"
fi
if [ -n "$CONTAINER_PG_PASS" ]; then
  echo "Using container POSTGRES_PASSWORD=(masked)"
  POSTGRES_PASSWORD="$CONTAINER_PG_PASS"
fi

# Export compose DB connection overrides to match the running container
export COMPOSE_DB_USERNAME="$POSTGRES_USER"
export COMPOSE_DB_PASSWORD="$POSTGRES_PASSWORD"
export COMPOSE_DB_URL="jdbc:postgresql://localhost:5432/${POSTGRES_DB}"

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
# Export Spring Boot datasource env vars so forked JVMs (Surefire) inherit them
export SPRING_DATASOURCE_URL="${COMPOSE_DB_URL}"
export SPRING_DATASOURCE_USERNAME="${COMPOSE_DB_USERNAME}"
export SPRING_DATASOURCE_PASSWORD="${COMPOSE_DB_PASSWORD}"

# Also pass properties explicitly to Maven as system properties. This helps when
# plugins or the build consult properties from the Maven session.
exec mvn -f "$ROOT_DIR/backend/pom.xml" \
  -DskipTests=false \
  -Dspring.datasource.url="${COMPOSE_DB_URL}" \
  -Dspring.datasource.username="${COMPOSE_DB_USERNAME}" \
  -Dspring.datasource.password="${COMPOSE_DB_PASSWORD}" \
  -Dtest="$TEST" test
