// `androidx.a2ui.compose:compose-runtime` 1.0.0-alpha01, ported to Compose Multiplatform. The
// Compose-side data model, component registry and JSON message parser. See vendor/README.md.
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  jvm()
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs { browser() }

  sourceSets {
    commonMain {
      dependencies {
        api(project(":vendor:a2ui-model"))
        api(project(":vendor:a2ui-engine"))
        api(libs.cmp.runtime)
        implementation(project(":vendor:a2ui-port-runtime"))
        implementation(libs.androidx.annotation)
        implementation(libs.androidx.collection)
      }
    }
  }
}
