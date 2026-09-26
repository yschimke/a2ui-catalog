pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "a2ui-catalog"

// The one place the catalog's A2UI plumbing lives: a synchronous surface host that draws a list of
// component payloads in a single composition, plus the deterministic media renderers a still
// capture needs. Both sheets below depend on it, so a sticker and a vendored sample are drawn by
// the
// same engine wiring. See a2ui-harness/build.gradle.kts.
include(":a2ui-harness")

// The A2UI basic catalog (v0.9.1), rendered component by component through
// `androidx.compose.material3:material3-a2ui` — every sticker is a real A2UI payload handed to the
// real Material catalog, never a hand-drawn imitation of what it would look like.
include(":catalog")

// The AndroidX A2UI samples: `material3-a2ui`'s own integration-test sample screens, vendored from
// a
// pinned androidx-main commit and rendered beside the catalog. Separate module, not a source set:
// the vendored sources are upstream's bytes under upstream's package, and must not be formatted,
// linted or refactored with this repo's own code. See docs/design/ANDROIDX_SAMPLES.md.
include(":samples-catalog")

// ── The Compose Multiplatform port of the A2UI release train, VENDORED
// ────────────────────────────
// `androidx.a2ui:*`, `androidx.a2ui.compose:*` and `material3-a2ui` are Android AARs; these
// modules are the same 1.0.0-alpha01 sources compiled for `jvm()` and `wasmJs`, published as
// `ee.schimke.a2uicmp:*` (vendor/a2ui-upstream.json owns the version). Upstream's bytes stay under
// upstream's `androidx.*` packages in each module's `src/commonMain`; every edit is listed in
// vendor/README.md. They are NOT wired into `:catalog` or `:samples-catalog`, which keep rendering
// the genuine AARs through Robolectric — the port is a separate artifact, not a substitute for what
// the sheets are evidence of.
//
// No repository is added for them: they are projects of this build. A consumer elsewhere reads the
// published tree from the `a2ui-cmp-maven` branch of `yschimke/a2ui-catalog-out`, and should fence
// that repository to `includeGroup("ee.schimke.a2uicmp")` so it can never answer an `androidx.*`
// request.
include(":vendor:a2ui-port-runtime")

include(":vendor:a2ui-model")

include(":vendor:a2ui-engine")

include(":vendor:a2ui-compose-runtime")

include(":vendor:a2ui-compose-ui")

include(":vendor:material3-a2ui")
