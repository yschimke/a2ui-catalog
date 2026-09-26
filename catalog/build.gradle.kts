// `:catalog` — the A2UI basic catalog (v0.9.1), as drawn by `material3-a2ui`, rebuilt as `@Preview`
// stickers.
//
// ── Why this IS an Android module ─────────────────────────────────────────────────────────────
//
// The sibling m3-catalog renders its kit through Compose Multiplatform desktop so preview.coo.ee
// can
// hold a live Skiko session against the bundle. That cannot apply here: the whole A2UI stack —
// `androidx.a2ui:*`, `androidx.a2ui.compose:*` and `material3-a2ui` — is published as AARs
// (`minCompileSdk=37`, `minCompileMinorSdk=1`) with no Compose Multiplatform port, so a desktop
// module cannot put it on the classpath at all. The choice is a Robolectric lane or no sheet, which
// is the choice m3-catalog's `:glimmer-catalog` made for the same reason.
//
// ── What a sticker is ─────────────────────────────────────────────────────────────────────────
//
// Every sticker is an A2UI PAYLOAD: the `updateComponents` list an agent would send, handed to the
// real `A2uiMessageProcessor` and drawn by the real `A2uiSurface`. Nothing here calls a Material
// composable to imitate what the catalog would draw, so a change in how the library renders a
// component moves the sheet, which is the point of having one.
plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.composePreview)
}

composePreview {
  // Robolectric ships shadows up to API 36, and 36 needs JDK 21+; 35 runs on the JDK 17 toolchain
  // this module compiles with. The pin every Android catalog module in the sibling repos carries.
  sdkVersion.set(35)
}

android {
  namespace = "ee.schimke.a2uicatalog"
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
  implementation(libs.composeai.preview.annotations)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlin.test.junit)
}
