// The narrow platform seam of the A2UI Compose Multiplatform port. NOT upstream code: upstream's
// sources reach `java.util`, `java.text`, `java.util.concurrent`, `android.util.JsonReader` and
// `androidx.core.util.PatternsCompat`, none of which exist in common Kotlin. This module re-declares
// exactly the members those call sites use, under the same simple names, so the vendored files
// change an import line and nothing else. See vendor/README.md.
plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
  jvm()
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs { browser() }

  compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

  sourceSets {
    commonMain { dependencies { implementation(libs.kotlinx.serialization.json) } }
    commonTest { dependencies { implementation(libs.kotlin.test) } }
  }
}
