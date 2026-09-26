// `:a2ui-harness` — the A2UI wiring both sheets draw through.
//
// A sticker in this repository is never a hand-drawn imitation of an A2UI component: it is a list
// of
// real `A2uiComponentPayload`s handed to the real Material 3 basic catalog through the real message
// processor. What this module adds is the part a still capture needs and an app does not:
//
// * a SYNCHRONOUS host. The documented wiring collects messages on `Dispatchers.Default` from a
//   `LaunchedEffect`, which is right for an app and wrong for a capture — the surface would arrive
//   some frames after the first one, on another thread, and whether it made the picture would be a
//   race. `StickerHost` runs the processor on `Dispatchers.Unconfined`, so a message has been fully
//   applied by the time `processMessage` returns and the surface exists in the FIRST composition.
// * DETERMINISTIC media. `Image`, `Video` and `AudioPlayer` take their renderers from the caller;
//   the AndroidX demo loads images with Coil over the network, which a reproducible sticker sheet
//   cannot depend on. `StickerMedia` draws each from constants.
//
// It declares no previews, so the compose-preview plugin is not applied here.
plugins {
  // No Kotlin plugin of its own: AGP 9 brings Kotlin support with it.
  alias(libs.plugins.android.library)
  alias(libs.plugins.compose.compiler)
}

android {
  namespace = "ee.schimke.a2uicatalog.harness"
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
  api(libs.a2ui.model)
  api(libs.a2ui.compose.runtime)
  api(libs.a2ui.compose.ui)
  api(libs.a2ui.material3)
  api(libs.androidx.compose.material3)
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.ui)

  testImplementation(libs.a2ui.engine)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlin.test.junit)
}
