#!/usr/bin/env bash
set -euo pipefail
TOKEN_FILE=".github_token"
if [ ! -f "$TOKEN_FILE" ]; then
  echo "token file missing" >&2
  exit 1
fi
TOKEN=$(cat "$TOKEN_FILE")
OWNER=duongtr4409
REPO=usm-copilot
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
for br in "${BRANCHES[@]}"; do
  echo "---"
  echo "Looking for PRs with head: $OWNER:$br"
  # Query PRs by head
  resp=$(curl -sS -G -H "Authorization: token $TOKEN" -H "Accept: application/vnd.github+json" --data-urlencode "head=$OWNER:$br" "https://api.github.com/repos/$OWNER/$REPO/pulls")
  # extract html_url and state
  pr_url=$(echo "$resp" | sed -n 's/.*"html_url"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' || true)
  pr_state=$(echo "$resp" | sed -n "s/.*\"state\"[[:space:]]*:[[:space:]]*\"\([^"]*\)\".*/\1/p" || true)
  if [ -z "$pr_url" ]; then
    echo "No PR found for $br"
  else
    echo "PR: $pr_url"
    echo "state: $pr_state"
  fi
done
