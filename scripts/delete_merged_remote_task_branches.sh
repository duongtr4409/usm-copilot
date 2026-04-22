#!/usr/bin/env bash
set -euo pipefail

OWNER=duongtr4409
REPO=usm-copilot

# Fetch and prune
git fetch --prune origin

# Determine remote branches merged into origin/master
merged=$(git branch -r --merged origin/master | sed 's/^[[:space:]]*origin\///' | grep -E '^TASK-|^task/TASK-' || true)

if [ -z "$merged" ]; then
  echo "No merged TASK-* remote branches found.";
  exit 0
fi

echo "Remote TASK branches merged into origin/master:"
echo "$merged"

# Delete each merged remote branch
for b in $merged; do
  echo "Deleting remote branch: $b"
  git push origin --delete "$b" || echo "Failed to delete remote branch: $b"
done

echo "Remote merged TASK-* branches cleanup complete."