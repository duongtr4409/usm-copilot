# Docker Compose deployment for AMS

Prereqs:
- Docker & Docker Compose installed
- Copy `.env.example` to `.env` and fill secrets

Build and run:

```bash
cp .env.example .env
docker compose build --no-cache
docker compose up -d --build
```

Check services:

```bash
docker compose ps
docker compose logs -f backend
```

Notes:
- Postgres will initialize with SQL files in `./db/migrations` on first run.
- The backend image mounts `./db/migrations` at `/app/db/migrations` so Flyway can pick them up if Flyway is enabled in the app.
- For development, run `docker compose up` (override applies) which will mount source and run maven/dev server.
**Docker Compose — Build & Run**

Overview
- This repository includes `docker-compose.yml` to run a local stack: Postgres (v15), Redis, backend (Spring Boot) and frontend (Vite -> nginx).
- Database migrations live in `db/migrations/` and are applied by Postgres initialization scripts and by Flyway in the backend.

Prerequisites
- Docker and Docker Compose (or Docker Desktop) installed.
- Copy the example env file: `cp .env.example .env` and update values for production.

Quick start (build and run)

```bash
# Build images and start containers in background
docker compose up -d --build

# View running services and health
docker compose ps

# Follow backend logs
docker compose logs -f backend
```

Local development (override)
- Use the override file to mount sources and run dev servers (Vite + Maven) for quicker iteration:

```bash
# This will pick up docker-compose.override.yml automatically when present
docker compose up --build

# Or to run only the frontend dev server and the DB:
docker compose up --build db redis frontend
```

Database migrations
- SQL files are under `db/migrations/` and are included in two ways:
  - Mounted into the Postgres container at `/docker-entrypoint-initdb.d`. Postgres will execute these scripts on first container initialization (empty DB).
  - Mounted into the backend at `/app/db/migrations` and Flyway (enabled in the backend) is configured to load `filesystem:/app/db/migrations` in addition to `classpath:db/migration`.

Notes
- The backend image runs the Spring Boot jar (`/app/app.jar`). The `SPRING_DATASOURCE_*` environment variables are used to connect to Postgres; update via `.env`.
- Frontend image serves built static assets from nginx. For local fast iteration use the override which runs the Vite dev server.

Troubleshooting
- If Postgres already has data, init scripts under `/docker-entrypoint-initdb.d` will not run — Flyway will still run migrations from the backend if it can connect and detect changes.
- If a service fails startup, inspect logs: `docker compose logs <service>` and check health with `docker compose ps`.

Integration tests & Docker API mismatch
-------------------------------------

Symptom: Testcontainers (docker-java) fails with an error like:

```
client version 1.32 is too old. Minimum supported API version is 1.40
```

Root cause: the docker-java client that Testcontainers uses negotiates a Docker
API version that is older than what the Docker daemon (server) requires. A
non-invasive workaround is to force the client to a compatible API version via
the `DOCKER_API_VERSION` environment variable.

Quick workaround (non-invasive)
- Use the repository wrapper which sets `DOCKER_API_VERSION` to `1.40` and
  runs the integration test class:

```bash
./scripts/run-integration-tests.sh
```

To override the API version (for newer servers):

```bash
DOCKER_API_VERSION=1.41 ./scripts/run-integration-tests.sh
```

Long-term / recommended fixes
- Upgrade Docker Engine on the development machine to a recent stable release
  (install from Docker's official repository). Example for Ubuntu/Debian:

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg lsb-release
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" |
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Verify server API version (use this to pick the right DOCKER_API_VERSION if needed):
docker version --format '{{.Server.APIVersion}}'
```

- In CI (GitHub Actions) you can export the env var for test jobs to avoid failures:

```yaml
jobs:
  backend-ci:
    runs-on: ubuntu-latest
    env:
      DOCKER_API_VERSION: "1.40"
    steps:
      # checkout, setup-java, etc.
```

- Alternatively, update Testcontainers/docker-java to a newer version in
  `backend/pom.xml` (bump Testcontainers dependencies to a recent release).

Notes
- The wrapper is intentionally conservative — it sets `DOCKER_API_VERSION` to
  `1.40` (a baseline compatible with recent Testcontainers releases). If your
  Docker server reports a higher API version you may set `DOCKER_API_VERSION`
  to match the server (see `docker version --format '{{.Server.APIVersion}}'`).

