#!/usr/bin/env bash
set -euo pipefail

# Switch to master and update
git checkout master
git pull --ff-only origin master || true
git fetch --prune origin

# Show merged TASK branches
echo "Merged local TASK branches (candidates for deletion):"
git branch --merged master | sed 's/^[* ]*//' | grep -E '^TASK-|^task/TASK-' || true

# Delete merged TASK branches
git branch --merged master | sed 's/^[* ]*//' | grep -E '^TASK-|^task/TASK-' | while read -r b; do
  if [ -n "$b" ] && [ "$b" != "master" ]; then
    echo "Deleting $b"
    git branch -d "$b" || echo "Failed to delete $b (not merged or protected)"
  fi
done

# List remaining local TASK branches (if any)
echo "Remaining local TASK branches (not deleted):"
git for-each-ref --format='%(refname:short)' refs/heads/ | grep -E '^TASK-|^task/TASK-' || true
