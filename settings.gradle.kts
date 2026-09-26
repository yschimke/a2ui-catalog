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
