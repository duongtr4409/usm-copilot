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
for pr in "$@"; do
  echo "---\nPR #$pr"
  curl -sS -H "Authorization: token $TOKEN" "https://api.github.com/repos/$OWNER/$REPO/pulls/$pr" | python3 -c 'import sys,json; obj=json.load(sys.stdin); print(obj.get("number"), obj.get("state"), obj.get("title")); print(obj.get("html_url")); print("head.ref=", obj.get("head",{}).get("ref")); print("base.ref=", obj.get("base",{}).get("ref"))'
done
