// `androidx.a2ui:a2ui-engine` 1.0.0-alpha01, ported to Compose Multiplatform. The message
// processor, surface models and schema validator. See vendor/README.md for the edits.
plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
  jvm()
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs { browser() }

  sourceSets {
    commonMain {
      dependencies {
        api(project(":vendor:a2ui-model"))
        implementation(project(":vendor:a2ui-port-runtime"))
        implementation(libs.androidx.annotation)
        implementation(libs.androidx.collection)
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.serialization.json)
      }
    }
  }
}
