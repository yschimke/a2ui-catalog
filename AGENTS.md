# AGENTS.md

Read [README.md](README.md) first. This file records the rules that are enforced by CI and the
conventions that are easy to break by accident. It inherits the house rules of
[yschimke/m3-catalog](https://github.com/yschimke/m3-catalog); where it differs, it is because A2UI
differs.

## CI-enforced

- **No agent attribution in git history.** No `Co-authored-by:` trailer naming an agent, and no
  commit whose author or committer identity is an agent. The PR title and body are scanned too.
  `.github/scripts/agent-attribution-scan.sh` is the one detector, run by the `commit-msg` and
  `pre-push` hooks (install with `scripts/install-git-hooks.sh`) and by the `No Agent Attribution`
  workflow. `Yuri Schimke <yuri@schimke.ee>` is this repository's human identity.
- **Branch names are `agent/…`**, never `claude/…`, `codex/…` or another agent prefix.
- **Conventional commits** for PR titles and commit subjects (`feat:`, `fix:`, `docs:`, `ci:`, …).
- **Run the formatter before committing** Kotlin: `./gradlew ktfmtFormat`. `ktfmtCheck` is a gate.
  The vendored tree under `samples-catalog/src/main/kotlin/upstream/` is excluded, on purpose.
- **Generated files are checked**: `a2ui-basic-catalog.schema.json` (`CatalogSchemaTest`),
  `ui-builder.policy.json`'s builtins (`scripts/ui-builder-policy.mjs --check`), `design-map.json`
  (`scripts/design-map.sh --check`), and the vendored samples (`scripts/import-samples.mjs --check`).
  Regenerate, never hand-edit.

## A sticker is a payload

Every `@Preview` in `:catalog` is an `A2uiSticker(listOf(component(...), ...))`: the component list
an agent would send, drawn by the real Material catalog. **Never** draw a Material composable to
stand in for what A2UI "would" draw: the point of the sheet is that it moves when the library does.
If the library draws something wrong, the sticker shows it and [docs/FINDINGS.md](docs/FINDINGS.md)
records it.

The root component's id is always `root`. Seed a bound value through `data` rather than leaving an
input empty, unless emptiness is the cell.

## One card per basic-catalog component

The inventory is the library's: `BasicCatalogComponents` lists the 18 components of A2UI basic
catalog v0.9.1, and `CatalogInventoryTest` fails if a card is missing, duplicated, or neither
`reference`d nor `noReference`d. A property like `variant`, `justify` or `displayStyle` is one
argument of one component, so its values are `@CatalogVariant` cells, never cards of their own.

A `reference` names the **M3 Design Kit** node of the Material component `material3-a2ui` actually
calls for that component (read the library source, not the name). A cell drawn by a DIFFERENT
Material component (the Button variants) names its own node. Figma is read-only.

## Determinism

`:a2ui-harness` exists so a capture is reproducible: `StickerHost` applies messages synchronously
(`StickerHostTest` holds it to that), and `StickerMedia` draws Image/Video/AudioPlayer from
constants. Do not load media from a URL, collect on `Dispatchers.Default`, or animate the surface's
entry in a sticker.

## The A2UI release train

`androidx.a2ui:*`, `androidx.a2ui.compose:*` and `material3-a2ui` move together, in one PR, and the
AndroidX Compose / material3 pins move with them to whatever `material3-a2ui`'s POM names. A bump
regenerates the schema; read its diff and the sticker diff.

## Running Gradle

Wrap Gradle in `build-brief`; on a shared host use `scripts/agent-gradle.sh` (four low-priority
workers). Needs an Android SDK with `platforms;android-37.1`.

## PR workflow

Open a PR when a change is committed and pushed. A PR that changes what a sticker draws carries
before/after renders as embedded images from a GitHub-hosted origin (commit-pinned
`raw.githubusercontent.com`). Do not merge your own PR.
