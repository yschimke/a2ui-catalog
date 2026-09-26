import groovy.json.JsonSlurper
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
  // Declared here, `apply false`, so AGP, the Kotlin plugins and the Compose compiler land on ONE
  // coordinated buildscript classpath. `:catalog`, `:samples-catalog` and `:a2ui-harness` are AGP
  // library modules: `material3-a2ui` and the A2UI runtime are published as AARs, so the sheets
  // render through Robolectric — the same situation m3-catalog's `:glimmer-catalog` is in. The
  // Kotlin Multiplatform and Compose Multiplatform plugins are for the vendored CMP port only
  // (vendor/README.md) and its desktop proof, `:a2ui-desktop`.
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.multiplatform) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.compose.multiplatform) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.composePreview) apply false
  alias(libs.plugins.ktfmt)
}

// The CMP port's version is derived from its pin, never typed: `<release>-cmp<portRevision>`.
val a2uiUpstream = JsonSlurper().parse(file("vendor/a2ui-upstream.json")) as Map<*, *>
val a2uiCmpPortVersion =
  "%s-cmp%02d".format(a2uiUpstream["release"], (a2uiUpstream["portRevision"] as Number).toInt())
val publishedA2uiCmpProjects =
  setOf(
    ":vendor:a2ui-port-runtime",
    ":vendor:a2ui-model",
    ":vendor:a2ui-engine",
    ":vendor:a2ui-compose-runtime",
    ":vendor:a2ui-compose-ui",
    ":vendor:material3-a2ui",
  )

allprojects {
  apply(plugin = "com.ncorti.ktfmt.gradle")
  ktfmt { googleStyle() }

  // AndroidX sources under vendor/ are pinned upstream bytes (AOSP style, not Google style).
  // Formatting them would turn a source import into a repository-wide rewrite and make the next
  // upstream comparison useless; the small port seam beside them follows their formatting.
  if (path.startsWith(":vendor:")) {
    tasks.matching { it.name.startsWith("ktfmt") }.configureEach { enabled = false }
  }
}

configure(publishedA2uiCmpProjects.map(::project)) {
  group = "ee.schimke.a2uicmp"
  version = a2uiCmpPortVersion
  apply(plugin = "maven-publish")

  extensions.configure<PublishingExtension> {
    publications.withType<MavenPublication>().configureEach {
      pom {
        name.set("A2UI Compose Multiplatform port: ${project.name}")
        description.set(
          "AndroidX A2UI ${a2uiUpstream["release"]} (${project.name}) compiled for Compose " +
            "Multiplatform (jvm, wasmJs). Not an AndroidX artifact; see vendor/README.md."
        )
        url.set("https://github.com/yschimke/a2ui-catalog/tree/main/vendor")
        licenses {
          license {
            name.set("The Apache Software License, Version 2.0")
            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
      }
    }
    repositories {
      // CI pushes this credential-free Maven tree to the `a2ui-cmp-maven` branch of
      // yschimke/a2ui-catalog-out. Keeping the local destination identical makes
      // publishA2uiCmpToBuildDir the exact preflight for CI.
      maven(rootProject.layout.buildDirectory.dir("a2ui-cmp-maven")) { name = "BuildDir" }
    }
  }
}

tasks.register("publishA2uiCmpToBuildDir") {
  group = "publishing"
  description = "Publish the vendored A2UI Compose Multiplatform port into build/a2ui-cmp-maven."
  dependsOn(publishedA2uiCmpProjects.map { "$it:publishAllPublicationsToBuildDirRepository" })
}

tasks.register("printA2uiCmpPortVersion") {
  group = "publishing"
  description = "Print the A2UI CMP port version derived from vendor/a2ui-upstream.json."
  inputs.property("a2uiCmpPortVersion", a2uiCmpPortVersion)
  doLast { println(inputs.properties.getValue("a2uiCmpPortVersion")) }
}
