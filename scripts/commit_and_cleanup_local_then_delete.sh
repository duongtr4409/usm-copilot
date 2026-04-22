#!/usr/bin/env bash
set -euo pipefail

BR=$(git rev-parse --abbrev-ref HEAD)
echo "Current branch: $BR"

git add -A
# commit if there are staged changes
if git diff --cached --quiet; then
  echo "No staged changes to commit"
else
  git commit -m "TASK-026: Persist PMO updates before local branch cleanup"
fi

# Switch to master
git checkout master

# List merged TASK branches
echo "Merged local TASK branches (candidates for deletion):"
git branch --merged master | sed 's/^[* ]*//' | grep -E '^TASK-|^task/TASK-' || true

# Delete merged TASK branches
git branch --merged master | sed 's/^[* ]*//' | grep -E '^TASK-|^task/TASK-' | while read -r b; do
  if [ -n "$b" ] && [ "$b" != "master" ]; then
    echo "Deleting $b"
    git branch -d "$b" || echo "Failed to delete $b (not merged or protected)"
  fi
done

# List remaining local TASK branches
echo "Remaining local TASK branches (not deleted):"
git for-each-ref --format='%(refname:short)' refs/heads/ | grep -E '^TASK-|^task/TASK-' || true
