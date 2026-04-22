#!/usr/bin/env bash
set -euo pipefail
TOKEN_FILE=".github_token"
if [ ! -f "$TOKEN_FILE" ]; then
  echo "token file missing" >&2
  exit 1
fi
TOKEN=$(cat "$TOKEN_FILE")
curl -sS -H "Authorization: token $TOKEN" "https://api.github.com/repos/duongtr4409/usm-copilot" | sed -n 's/.*"default_branch"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
