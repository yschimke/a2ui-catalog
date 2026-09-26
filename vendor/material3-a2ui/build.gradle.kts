// `androidx.compose.material3:material3-a2ui` 1.0.0-alpha01, ported to Compose Multiplatform: the
// Material 3 basic catalog, drawn by Compose Multiplatform material3. See vendor/README.md.
plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

kotlin {
  // `DatePickerDialog` is experimental in CMP material3 1.13.0-alpha01 (AndroidX 1.5.0-alpha27) and
  // stable in the 1.5.0-alpha28 upstream compiled against. An opt-in here, not in the sources.
  compilerOptions { optIn.add("androidx.compose.material3.ExperimentalMaterial3Api") }
  jvm()
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs { browser() }

  sourceSets {
    commonMain {
      // Port-only sources (the `R` / `stringResource` stand-ins) live beside upstream's bytes, not
      // among them, so `src/commonMain` stays a pure upstream tree.
      kotlin.srcDir("src/commonPort/kotlin")
      dependencies {
        api(project(":vendor:a2ui-model"))
        api(project(":vendor:a2ui-compose-runtime"))
        api(project(":vendor:a2ui-compose-ui"))
        api(libs.cmp.animation)
        implementation(project(":vendor:a2ui-port-runtime"))
        implementation(project(":vendor:a2ui-engine"))
        implementation(libs.cmp.foundation)
        implementation(libs.cmp.material3)
        implementation(libs.cmp.runtime)
        implementation(libs.cmp.ui)
        implementation(libs.androidx.collection)
      }
    }
  }
}
