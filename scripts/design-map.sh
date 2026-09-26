#!/usr/bin/env bash
# Project `design-map.json` (and its variants sidecar) from the @CatalogComponent / @CatalogVariant
# annotations — the Figma join the design-artifacts lane and design-parity read.
#
# Every `reference` names a node of the Material 3 Design Kit, because `material3-a2ui` draws the
# basic catalog with Material 3 components. The components the kit has no node for carry a
# `noReference`, and `--allow-stated-absence` is what accepts exactly those: an unmapped component
# WITHOUT a stated reason still fails `--strict`, so "nobody looked" cannot pass for "the kit has
# none".
#
#   ./gradlew :catalog:composePreviewDiscover && scripts/design-map.sh          # regenerate
#   ./gradlew :catalog:composePreviewDiscover && scripts/design-map.sh --check  # CI: fail if stale
set -euo pipefail

CHECK=""
[ "${1:-}" = "--check" ] && CHECK=1

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

npx --yes @yschimke/compose-design-map@1.55.0 \
  --previews catalog/build/compose-previews/previews.json \
  --out "$WORK/design-map.json" \
  --variants "$WORK/design-map-variants.json" \
  --strict --allow-stated-absence

for f in design-map.json design-map-variants.json; do
  if [ -n "$CHECK" ]; then
    if [ -f "$WORK/$f" ]; then
      diff -q "$WORK/$f" "$f" >/dev/null 2>&1 || {
        echo "::error::$f is out of date — regenerate with scripts/design-map.sh"
        exit 1
      }
    elif [ -f "$f" ]; then
      echo "::error::$f is stale and should be removed — regenerate with scripts/design-map.sh"
      exit 1
    fi
  elif [ -f "$WORK/$f" ]; then
    cp "$WORK/$f" "$f"
  else
    rm -f "$f"
  fi
done

[ -n "$CHECK" ] && echo "✓ design-map.json and its sidecar match the annotations."
exit 0
