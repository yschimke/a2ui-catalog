// `:a2ui-desktop` — the proof the vendored Compose Multiplatform port (vendor/) renders A2UI on the
// desktop JVM with no Robolectric and no Android API on the classpath.
//
// `DesktopA2uiHost` is `:a2ui-harness`'s synchronous `StickerHost` re-expressed against the port;
// `DesktopRenderTest` hands a real createSurface + updateComponents payload to it, draws the
// surface with the vendored Material 3 basic catalog under Skiko, and asserts on the pixels and the
// semantics. It writes the PNG committed as docs/evidence/cmp-desktop-render.png.
//
// Not published, declares no previews: the sticker sheets stay on the genuine AARs (see README).
plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
}

dependencies {
  implementation(project(":vendor:material3-a2ui"))
  implementation(project(":vendor:a2ui-engine"))
  implementation(libs.cmp.material3)
  implementation(libs.cmp.foundation)
  implementation(libs.kotlinx.coroutines.core)
  implementation(compose.desktop.currentOs)

  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.cmp.ui.test)
}

tasks.test {
  // Where the render lands; the test copies nothing into docs/ by itself.
  systemProperty("a2ui.desktop.out", layout.buildDirectory.dir("a2ui-desktop").get().asFile.path)
}
