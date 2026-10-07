// `:samples-catalog` — the AndroidX A2UI samples, rendered.
//
// Everything under `src/main/kotlin/upstream/` is VENDORED, in upstream's own package
// (`androidx.compose.material3.integration.a2ui`), fetched by `scripts/import-samples.mjs` from the
// commit pinned in `samples-catalog/import.json`. It is never edited in place and never formatted —
// a
// fix is a patch in `samples-catalog/patches/` with a stated reason.
//
// ── What a sample is here ─────────────────────────────────────────────────────────────────────
//
// The A2UI corpus is not a set of `@Sampled` one-liners. Each `<Component>Sample` in
// material3-a2ui's
// integration-test demo is a CONTROL PANEL: chips, switches and text inputs that compose an A2UI
// payload and hand it to a callback. The demo draws that payload above the panel. `SampleScreen.kt`
// reproduces that screen — preview card over controls — on the synchronous `:a2ui-harness` host,
// and
// `SamplePreviews.kt` publishes one `@Preview` per component. So each render shows both halves:
// the controls an engineer would turn, and exactly what the Material catalog draws for the payload
// they produce at their initial settings.
plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.composePreview)
}

composePreview {
  // The pin `:catalog` carries, for the same reason: Robolectric ships shadows to API 36 and 36
  // needs JDK 21+, while 35 runs on the JDK 17 toolchain these modules compile with.
  sdkVersion.set(35)
}

android {
  namespace = "ee.schimke.a2uicatalog.samples"
  // `material3-a2ui` 1.0.0-alpha01's AAR metadata: `minCompileSdk=37`, `minCompileMinorSdk=1`.
  compileSdk { version = release(37) { minorApiLevel = 1 } }
  defaultConfig { minSdk = 24 }

  buildFeatures { compose = true }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(project(":a2ui-harness"))
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.ui.tooling)
  implementation(platform(libs.composeai.daemon.bom))
  implementation(libs.composeai.preview.annotations)
  implementation(libs.composeai.preview.overrides)

  testImplementation(libs.robolectric)
  testImplementation(libs.kotlin.test.junit)
}

// The vendored tree is upstream's bytes. ktfmt would rewrite it into a permanent diff against every
// future import, so it is excluded — the formatting counterpart of "a fix is a patch, never an
// edit".
tasks.withType<com.ncorti.ktfmt.gradle.tasks.KtfmtBaseTask>().configureEach {
  exclude { it.file.absolutePath.replace('\\', '/').contains("/src/main/kotlin/upstream/") }
}
