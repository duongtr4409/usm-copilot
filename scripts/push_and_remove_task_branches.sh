#!/usr/bin/env bash
set -euo pipefail

# Push local commits for TASK branches that are ahead of origin, then delete all local TASK branches

for b in $(git for-each-ref --format='%(refname:short)' refs/heads | grep -E '^TASK-|^task/TASK-' || true); do
  # skip master just in case
  if [ "$b" = "master" ]; then
    continue
  fi
  echo "Processing branch: $b"
  if git show-ref --verify --quiet refs/remotes/origin/$b; then
    counts=$(git rev-list --left-right --count origin/$b...$b 2>/dev/null || echo "0 0")
    ahead=$(echo "$counts" | awk '{print $2}')
    if [ "$ahead" -gt 0 ]; then
      echo "Pushing $b (ahead $ahead commits) to origin..."
      git push origin "$b" || echo "Push failed for $b"
    else
      echo "$b is not ahead of origin"
    fi
  else
    echo "No origin/$b - pushing as new branch..."
    git push -u origin "$b" || echo "Push failed for $b"
  fi
done

# Delete local TASK branches
for b in $(git for-each-ref --format='%(refname:short)' refs/heads | grep -E '^TASK-|^task/TASK-' || true); do
  echo "Deleting local branch: $b"
  git branch -D "$b" || echo "Failed to delete $b"
done

echo "Local TASK branch cleanup finished."
