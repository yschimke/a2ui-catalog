#!/usr/bin/env bash
# Fails when a change to the PUBLISHED bytes of the A2UI Compose Multiplatform port (vendor/) does
# not increase `portRevision` in vendor/a2ui-upstream.json. A published `<release>-cmpNN` version is
# immutable: the publish workflow skips a version that already exists on the Maven branch, so an
# un-bumped change would silently never ship — or, republished, would change bytes under a
# coordinate consumers have already resolved.
#
# Usage: check-a2ui-cmp-port-revision.sh [<base-ref>]   (default HEAD^1)
#
# What counts as published: every file under the six published modules, except tests and READMEs.
# The pin itself and vendor/README.md are not in the list; build wiring that shapes the published
# POM/module metadata lives in each module's build.gradle.kts, which IS.
set -euo pipefail

base=${1:-HEAD^1}
metadata=vendor/a2ui-upstream.json

published=$(git diff --name-only "$base" HEAD -- \
  'vendor/a2ui-port-runtime/**' \
  'vendor/a2ui-model/**' \
  'vendor/a2ui-engine/**' \
  'vendor/a2ui-compose-runtime/**' \
  'vendor/a2ui-compose-ui/**' \
  'vendor/material3-a2ui/**' \
  | grep -Ev '(^|/)(src/[^/]*Test/|README\.md$)' || true)

if [[ -z "$published" ]]; then
  exit 0
fi

head_revision=$(python3 -c "import json; print(json.load(open('$metadata'))['portRevision'])")
if git cat-file -e "$base:$metadata" 2>/dev/null; then
  base_revision=$(git show "$base:$metadata" | python3 -c 'import json,sys; print(json.load(sys.stdin)["portRevision"])')
else
  # The port does not exist at the base: any revision is a first publication.
  base_revision=0
fi

if (( head_revision <= base_revision )); then
  echo "Published A2UI CMP port sources changed without increasing portRevision." >&2
  echo "Bump portRevision in $metadata (published versions are immutable)." >&2
  echo "Base: $base_revision; head: $head_revision" >&2
  echo "$published" >&2
  exit 1
fi
echo "portRevision $base_revision -> $head_revision covers:"
echo "$published"
