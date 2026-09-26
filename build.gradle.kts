plugins {
  // Declared here, `apply false`, so AGP and the Compose compiler land on ONE coordinated
  // buildscript classpath. Every module in this build is an AGP library module: `material3-a2ui`
  // and the A2UI runtime are published as AARs with no Compose Multiplatform port, so the render
  // lane is Robolectric or nothing — the same situation m3-catalog's `:glimmer-catalog` is in.
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.composePreview) apply false
  alias(libs.plugins.ktfmt)
}

allprojects {
  apply(plugin = "com.ncorti.ktfmt.gradle")
  ktfmt { googleStyle() }
}
