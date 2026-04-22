#!/usr/bin/env bash
set -euo pipefail
OWNER=duongtr4409
REPO=usm-copilot
TOKEN_FILE=".github_token"
if [ ! -f "$TOKEN_FILE" ]; then
  echo "Token file not found: $TOKEN_FILE" >&2
  exit 1
fi
TOKEN=$(cat "$TOKEN_FILE")
BRANCHES=(
  "TASK-016/fix-testcontainers-docker-java"
  "TASK-017/fix-compose-credentials"
  "TASK-018/fix-enrollment-table-name"
  "TASK-019/disable-security-for-tests"
  "TASK-020/fix-enrollments-fk"
  "TASK-021/fix-outbox-payload-type"
  "TASK-022/add-ci-workflow"
  "TASK-024/add-non-destructive-migrations"
)

TITLES=(
  "TASK-016: Fix Testcontainers Docker Java compatibility"
  "TASK-017: Fix compose-runner credential propagation"
  "TASK-018: Fix enrollment table name"
  "TASK-019: Disable security for integration tests"
  "TASK-020: Fix enrollments foreign key"
  "TASK-021: Fix outbox payload type"
  "TASK-022: Add CI workflow"
  "TASK-024: Add non-destructive migrations"
)

BODIES=(
  "Fix compatibility between Testcontainers and docker-java. See logs/TASK-016/try-testcontainers-output.txt"
  "Ensure compose-runner credentials are propagated to Maven forks. See logs/TASK-017/runner-output.log"
  "Align JPA Enrollment entity/table name with DB migrations. See logs/TASK-018/runner-output.log"
  "Add an integration-tests profile to relax security for integration runs. See logs/TASK-019/runner-output.log"
  "Ensure enrollments.class_unit_id references organization_unit(id). See logs/TASK-020/runner-output.log"
  "Align outbox.payload to TEXT to match JPA mapping. See logs/TASK-021/runner-output.log"
  "Add GitHub Actions workflow for compose-backed integration tests. See logs/TASK-022/maven-unit-tests.log"
  "Add idempotent migrations V13 and V14 to align enrollments and outbox without editing historic files."
)

for ((i=0;i<${#BRANCHES[@]};i++)); do
  br="${BRANCHES[$i]}"
  title="${TITLES[$i]}"
  body="${BODIES[$i]}"
  head="$OWNER:$br"
  echo "---"
  echo "Creating PR for branch: $br"
  # Build JSON payload safely (escape double quotes)
  esc_title=${title//\"/\\\"}
  esc_body=${body//\"/\\\"}
  payload=$(printf '{"title":"%s","head":"%s","base":"master","body":"%s"}' "$esc_title" "$head" "$esc_body")
  resp=$(curl -sS -X POST -H "Authorization: token $TOKEN" -H "Accept: application/vnd.github+json" -d "$payload" "https://api.github.com/repos/$OWNER/$REPO/pulls" ) || { echo "curl failed for $br"; echo "$resp"; continue; }
  pr_url=$(echo "$resp" | sed -n 's/.*"html_url"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' || true)
  pr_num=$(echo "$resp" | sed -n 's/.*"number"[[:space:]]*:[[:space:]]*\([0-9]*\).*/\1/p' || true)
  if [ -z "$pr_url" ]; then
    echo "FAILED creating PR for $br: $resp"
    continue
  fi
  echo "Created: $pr_url"
  # add labels (ignore failures)
  curl -sS -X POST -H "Authorization: token $TOKEN" -H "Accept: application/vnd.github+json" -d '["integration-tests","devops"]' "https://api.github.com/repos/$OWNER/$REPO/issues/$pr_num/labels" >/dev/null || echo "labels add failed"
  # request reviewers (ignore failures)
  curl -sS -X POST -H "Authorization: token $TOKEN" -H "Accept: application/vnd.github+json" -d "{\"reviewers\":[\"Code-Review\",\"Java-BE\",\"DB-Admin\"]}" "https://api.github.com/repos/$OWNER/$REPO/pulls/$pr_num/requested_reviewers" >/dev/null || echo "request reviewers failed"
  echo "$br -> $pr_url"
  sleep 0.4
done

echo "PR creation complete."
