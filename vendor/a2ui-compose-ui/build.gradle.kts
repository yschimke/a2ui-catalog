// `androidx.a2ui.compose:compose-ui` 1.0.0-alpha01, ported to Compose Multiplatform. The catalog
// API (`A2uiCatalog`, `A2uiComponent`) and the basic catalog v1 contract. See vendor/README.md.
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
        api(project(":vendor:a2ui-compose-runtime"))
        api(libs.cmp.runtime)
        api(libs.cmp.ui)
        implementation(project(":vendor:a2ui-port-runtime"))
        implementation(project(":vendor:a2ui-engine"))
        implementation(libs.androidx.collection)
      }
    }
  }
}
