// `androidx.a2ui:a2ui-model` 1.0.0-alpha01, ported to Compose Multiplatform. The protocol model,
// schemas and basic-catalog functions. See vendor/README.md for the edits.
plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
  jvm()
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs { browser() }

  sourceSets {
    commonMain {
      dependencies {
        // `Locale` is in this module's public API, so the port seam is too.
        api(project(":vendor:a2ui-port-runtime"))
        // `StateFlow` / `Flow` are in `A2uiMessageProcessor`'s API. Upstream reaches coroutines through
        // `androidx.core` on Android; here it is declared.
        api(libs.kotlinx.coroutines.core)
        implementation(libs.androidx.annotation)
        implementation(libs.androidx.collection)
        implementation(libs.kotlinx.serialization.json)
      }
    }
    commonTest { dependencies { implementation(libs.kotlin.test) } }
  }
}
