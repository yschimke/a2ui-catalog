# The AndroidX A2UI samples

## Where they come from

AndroidX publishes no samples artifact for A2UI (as for every library: sample modules are not
released). The KDoc samples of `material3-a2ui` (`A2uiSurfaceSamples.kt`) are three variations of
"create a surface and send it one Text", two of which delay two seconds before sending; they are not
imported (see `samples-catalog/import.json`).

The real per-component corpus is `compose/material3/integration-tests/material3-a2ui`, the demo app.
Its `ui/samples/<Component>Sample.kt` files are control panels that each compose one component's
payload and hand it to a callback; `ComponentDetailScreen` draws the payload above them.

## How they are vendored

The m3-catalog contract, unchanged, with the same script (`scripts/import-samples.mjs`):

- **Pinned to a commit SHA**, never a branch: `samples-catalog/import.json`.
- **Upstream's bytes, in upstream's package** under `samples-catalog/src/main/kotlin/upstream/`,
  never edited or formatted. ktfmt excludes the tree.
- **A fix is a patch** in `samples-catalog/patches/` with its reason. There are none.
- **A file that cannot be imported is quarantined** with its reason in
  `samples-catalog/quarantine.json`: the Activity, the detail screen (it collects messages on
  `Dispatchers.Default` and loads images from the network), and the Coil image renderer. `--check`
  fails if one becomes importable again.
- `PROVENANCE.json` beside the tree records the commit and file count.

## How they are rendered

`SampleScreen.kt` is the demo's compact layout (preview card over controls) on the `:a2ui-harness`
host instead of the demo's asynchronous one, and `SamplePreviews.kt` publishes one `@Preview` per
`UiComponent`, plus the demo's index screen. Each preview carries a `@CatalogComponent` whose
`related` names the matching `a2ui-catalog` card, so the served sheets link sticker and sample.
