#!/usr/bin/env bash
set -euo pipefail

git checkout master

echo "Merged local TASK branches (candidates for deletion):"
git branch --merged master | sed 's/^[* ]*//' | grep -E '^TASK-|^task/TASK-' || true

git branch --merged master | sed 's/^[* ]*//' | grep -E '^TASK-|^task/TASK-' | while read -r b; do
  if [ -n "$b" ] && [ "$b" != "master" ]; then
    echo "Deleting $b"
    git branch -d "$b" || echo "Failed to delete $b (not merged or protected)"
  fi
done

echo "Remaining local TASK branches (not deleted):"
git for-each-ref --format='%(refname:short)' refs/heads/ | grep -E '^TASK-|^task/TASK-' || true
