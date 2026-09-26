// `:a2ui-ui-builder-renderer` — the A2UI catalog's UI-builder renderer runtime.
//
// The ui-builder canvas cannot link `material3-a2ui` (an Android AAR), so without this every A2UI
// component in the editor is a labelled outline. This module is the separately built add-on the
// editor loads instead: a Wasm page, in an `<iframe sandbox="allow-scripts">`, that lowers the
// design to A2UI messages (compose-ui-builder's `A2uiDocumentExporter`, the same lowering the JSON
// export uses) and draws them through the vendored CMP port's Material 3 basic catalog.
//
// Its only contract with the editor is the renderer SDK's postMessage protocol and the
// `runtime-manifest.json` written below; the Design Artifacts workflow publishes the ZIP beside
// catalog.json. Included only with -PcomposeUiBuilderDir (see settings.gradle.kts). The assembly
// matches yschimke/wear-m3-catalog's `:catalog-ui-builder-renderer`.
import java.security.MessageDigest
import java.util.zip.ZipFile

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

val runtimeIdentity =
  providers
    .environmentVariable("GITHUB_SHA")
    .map { "a2ui-p2-${it.take(12)}" }
    .orElse("a2ui-p2-development")

kotlin {
  @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
  wasmJs {
    browser()
    outputModuleName.set("a2uiCatalogRenderer")
    binaries.executable()
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.composeai.ui.builder.renderer.sdk.source)
      implementation(project(":vendor:material3-a2ui"))
      implementation(project(":vendor:a2ui-engine"))
      implementation(libs.cmp.foundation)
      implementation(libs.cmp.material3)
      implementation(libs.cmp.runtime)
      implementation(libs.cmp.ui)
      implementation(libs.kotlinx.coroutines.core)
    }
  }
}

val uiBuilderCheckout =
  providers.gradleProperty("composeUiBuilderDir").map { rootProject.file(it).canonicalFile }

val runtimeAssets =
  tasks.register<Sync>("runtimeAssets") {
    // Compose Multiplatform 1.13 renamed 1.12's `processSkikoRuntimeForKWasm` and its output.
    dependsOn("wasmJsProductionExecutableCompileSync", "unpackSkikoRuntimeForWasmJs")
    dependsOn("wasmJsProcessResources")
    // The Binaryen-optimized production binary: about 7 MB against the development build's 55 MB,
    // and the editor loads it into every A2UI design's canvas.
    from(layout.buildDirectory.dir("compileSync/wasmJs/main/productionExecutable/optimized")) {
      exclude("*.map")
    }
    from(layout.buildDirectory.dir("compose/skiko-wasmJs-runtime")) {
      include("skiko.mjs", "skiko.wasm")
    }
    from(layout.buildDirectory.dir("kotlin-multiplatform-resources/aggregated-resources/wasmJs"))
    from(layout.projectDirectory.dir("src/wasmJsMain/resources")) { include("index.html") }
    // Skiko's browser loader and the renderer's typography consume these at runtime. They are
    // assets, not executable UI Builder code; the renderer links only the source/composite SDK.
    // Skiko on Wasm has no system fonts, so without Roboto every A2UI Text draws nothing.
    from(uiBuilderCheckout.map { it.resolve("assets/js-joda") }) { include("js-joda.esm.js") }
    from(uiBuilderCheckout.map { it.resolve("assets/rc-fonts") }) {
      include("Roboto-Regular.ttf", "Roboto-Medium.ttf", "LICENSE.txt", "README.md")
      into("fonts")
    }
    into(layout.buildDirectory.dir("runtimeAssets"))
  }

abstract class AssembleCatalogRendererRuntime : DefaultTask() {
  @get:InputDirectory
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val assetsDirectory: DirectoryProperty

  @get:Input abstract val runtimeId: Property<String>

  @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

  @get:Inject abstract val fileSystemOperations: FileSystemOperations

  @TaskAction
  fun assemble() {
    val source = assetsDirectory.get().asFile
    val output = outputDirectory.get().asFile
    fileSystemOperations.sync {
      from(source)
      into(output)
    }
    val digest = MessageDigest.getInstance("SHA-256")
    output
      .walkTopDown()
      .filter(File::isFile)
      .map { it.relativeTo(output).invariantSeparatorsPath to it.readBytes() }
      .sortedWith { left, right -> compareUnsignedUtf8(left.first, right.first) }
      .forEach { (path, bytes) ->
        digest.update(path.encodeToByteArray())
        digest.update(0)
        digest.update(bytes.size.toString().encodeToByteArray())
        digest.update(0)
        digest.update(bytes)
      }
    val integrity = digest.digest().joinToString("") { "%02x".format(it) }
    output
      .resolve("runtime-manifest.json")
      .writeText(
        """{"schema":"compose-ui-builder-runtime/v1","runtimeId":"${runtimeId.get()}","protocolVersion":2,"entrypoint":"index.html","integritySha256":"$integrity"}"""
      )
  }

  private fun compareUnsignedUtf8(left: String, right: String): Int {
    val leftBytes = left.encodeToByteArray()
    val rightBytes = right.encodeToByteArray()
    val shared = minOf(leftBytes.size, rightBytes.size)
    for (index in 0 until shared) {
      val difference = (leftBytes[index].toInt() and 0xff) - (rightBytes[index].toInt() and 0xff)
      if (difference != 0) return difference
    }
    return leftBytes.size - rightBytes.size
  }
}

val wasmRendererDist =
  tasks.register<AssembleCatalogRendererRuntime>("wasmRendererDist") {
    description = "Assemble the catalog-owned A2UI UI-builder renderer."
    group = "distribution"
    dependsOn(runtimeAssets)
    assetsDirectory.set(layout.buildDirectory.dir("runtimeAssets"))
    runtimeId.set(runtimeIdentity)
    outputDirectory.set(layout.buildDirectory.dir("wasmRendererDist"))
  }

val rendererArchive =
  tasks.register<Zip>("rendererArchive") {
    description = "Package the immutable catalog-owned A2UI UI-builder renderer."
    group = "distribution"
    dependsOn(wasmRendererDist)
    from(wasmRendererDist.flatMap { it.outputDirectory })
    archiveFileName.set("a2ui-ui-builder-renderer.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
  }

abstract class VerifyCatalogRendererRuntime : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val archiveFile: RegularFileProperty

  @get:Input abstract val expectedRuntimeId: Property<String>

  @TaskAction
  fun verify() {
    ZipFile(archiveFile.get().asFile).use { zip ->
      val names = zip.entries().asSequence().map { it.name }.toSet()
      val required =
        setOf(
          "runtime-manifest.json",
          "index.html",
          "a2uiCatalogRenderer.mjs",
          "a2uiCatalogRenderer.wasm",
          "fonts/Roboto-Regular.ttf",
          "fonts/Roboto-Medium.ttf",
          "skiko.mjs",
          "skiko.wasm",
        )
      check(names.containsAll(required)) { "renderer archive is missing ${required - names}" }
      check(names.none { it.startsWith('/') || it.contains("../") || '\\' in it }) {
        "renderer archive contains an unsafe path"
      }
      val manifest = zip.getInputStream(zip.getEntry("runtime-manifest.json")).reader().readText()
      check(manifest.contains("\"runtimeId\":\"${expectedRuntimeId.get()}\""))
      check(manifest.contains("\"protocolVersion\":2"))
      check(manifest.contains(Regex("\"integritySha256\":\"[a-f0-9]{64}\"")))
    }
  }
}

tasks.register<VerifyCatalogRendererRuntime>("verifyRendererRuntime") {
  group = "verification"
  dependsOn(rendererArchive)
  archiveFile.set(rendererArchive.flatMap { it.archiveFile })
  expectedRuntimeId.set(runtimeIdentity)
}

tasks.named("check") { dependsOn("verifyRendererRuntime") }
