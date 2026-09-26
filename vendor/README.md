# Vendored A2UI, ported to Compose Multiplatform

AndroidX publishes the A2UI release train (`androidx.a2ui:*`, `androidx.a2ui.compose:*`,
`androidx.compose.material3:material3-a2ui`) as Android AARs only. These modules are the same
**1.0.0-alpha01** sources compiled for Compose Multiplatform: `jvm()` (desktop, Skiko) and
`wasmJs { browser() }`. There is no Android target: an Android consumer keeps the real AARs, and so
does this repository's own sticker sheets (`:catalog`, `:samples-catalog` render the genuine library
through Robolectric and do not depend on anything here).

## Pin and source

[`a2ui-upstream.json`](a2ui-upstream.json) pins the port:

- **`release: 1.0.0-alpha01`, `source: sources-jars`.** The Kotlin under each module's
  `src/commonMain/kotlin` is the release's own published `-sources.jar`, unpacked byte for byte (the
  jars' SHA-256s are in the pin). The first commit on the port's history imports them unmodified, so
  `git diff <that commit> -- vendor/*/src/commonMain` is exactly the set of edits listed below.
- **Not androidx-main.** `androidxMainReference.commit` (`e6bfe34…`) was compared and has moved on
  in behaviour, not just docs: `A2uiObjectSchema` sorts `required`, the engine aliases the v0_9_1
  basic-catalog id, `A2uiBasicCatalogV1.CatalogId` stopped being `const`, and the Material `Button`
  and `TextField` lay out differently. Vendoring main would have ported a library nobody released,
  so the pin records main only as a reference and lists those differences.
- **`portRevision: 1`.** Published versions are `<release>-cmp<portRevision>`, so this is
  `1.0.0-alpha01-cmp01`. They are immutable: bump `portRevision` whenever published bytes change
  without moving to a newer release. CI enforces it
  ([`check-a2ui-cmp-port-revision.sh`](../.github/scripts/check-a2ui-cmp-port-revision.sh)).

## Modules

| Local module | Published as (`ee.schimke.a2uicmp:`) | Upstream artifact |
| --- | --- | --- |
| `:vendor:a2ui-port-runtime` | `a2ui-port-runtime` | none: the port seam, see below |
| `:vendor:a2ui-model` | `a2ui-model` | `androidx.a2ui:a2ui-model` |
| `:vendor:a2ui-engine` | `a2ui-engine` | `androidx.a2ui:a2ui-engine` |
| `:vendor:a2ui-compose-runtime` | `a2ui-compose-runtime` | `androidx.a2ui.compose:compose-runtime` |
| `:vendor:a2ui-compose-ui` | `a2ui-compose-ui` | `androidx.a2ui.compose:compose-ui` |
| `:vendor:material3-a2ui` | `material3-a2ui` | `androidx.compose.material3:material3-a2ui` |

Packages are upstream's (`androidx.a2ui.*`, `androidx.compose.material3.a2ui.*`), so code written
against the AARs compiles against the port unchanged. The Compose closure is Compose Multiplatform
**1.13.0-alpha01** (runtime/ui/foundation/animation/material3), whose Android variants re-export
AndroidX Compose 1.13.0-alpha02 and material3 1.5.0-alpha27 — one alpha behind the 1.13.0-alpha03 /
1.5.0-alpha28 `material3-a2ui`'s POM names, and the closest line that exists. `androidx.collection`
1.6.0, `androidx.annotation` 1.10.0, `kotlinx-serialization-json` and `kotlinx-coroutines` are the
POMs' own, all multiplatform. ktfmt is disabled for `:vendor:*` (root `build.gradle.kts`).

### `a2ui-port-runtime`: the seam

Upstream reaches `java.util`, `java.text`, `java.util.concurrent`, `android.util.JsonReader` and
`androidx.core.util.PatternsCompat`. The seam (package `ee.schimke.a2uicmp.port`) re-declares
**exactly the members those call sites use, under the same simple names**, so almost every upstream
edit is one import line:

| Seam type | Stands in for | JVM | wasmJs |
| --- | --- | --- | --- |
| `Locale` (`expect`) | `java.util.Locale` | `actual typealias` to it: the JVM API is binary-identical to the AARs' | `Locale(languageTag)`, default `en-US` |
| `Locales.US` / `getDefault()` / `forLanguageTag()` | the `Locale` statics | `java.util.Locale` | `en-US` |
| `Date`, `TimeZone`, `DateFormat`, `SimpleDateFormat`, `ParseException`, `Calendar` | `java.util` / `java.text` | wrap the real `java.text` / `java.util` classes | a small en-US, UTC, Gregorian implementation (`WasmTime.kt`) |
| `NumberFormat`, `Currency` | `java.text` / `java.util` | wrap the real classes | en-US grouping, half-even rounding, `$ € £ ¥` symbols |
| `ConcurrentHashMap`, `AtomicInteger`, `ReentrantLock` + `withLock` | `java.util.concurrent`, `kotlin.concurrent.withLock` | the real classes (`typealias` / delegate) | plain, single-threaded |
| `System.currentTimeMillis` / `identityHashCode`, `Character.*` | `java.lang` | `java.lang` | `Date.now()`, `hashCode()`, Kotlin char categories |
| `putIfAbsent` | `java.util.Map.putIfAbsent` | the JDK member wins over the extension | extension |
| `JsonReader`, `JsonToken`, `StringReader` | `android.util.JsonReader` | common: a pull reader over a `kotlinx.serialization` JSON tree | same |
| `PatternsCompat.EMAIL_ADDRESS` | `androidx.core.util.PatternsCompat` | the regex copied verbatim from `PatternsCompat.java` | same |
| `is24HourFormat()` | `android.text.format.DateFormat.is24HourFormat(Context)` | default locale's short time pattern has `H` | `false` |

On the JVM, therefore, formatting, parsing, locking and hashing behave as upstream's do on Android.
The one behavioural difference there is the JSON reader: it parses the whole message when created,
not token by token. Upstream creates its readers inside the `try` that maps any failure to
`A2uiValidationException`, so a caller sees the same error. `PortRuntimeTest` holds the seam to its
contract.

## Every edit to upstream's files

Greppable three ways: `ee.schimke.a2uicmp.port` (seam imports), `CMP-PORT` (every edit that is not
an import line) and `import kotlin.jvm.` (added imports). 67 of 187 files change; nothing else does.

1. **Import swaps to the seam** (50 import lines across 20 files). `import java.util.Locale` becomes
   `import ee.schimke.a2uicmp.port.Locale`, and likewise for every row of the table above. Two
   seam objects, `System` and `Character`, are imported where upstream used `java.lang`'s implicitly;
   an explicit import shadows the implicit one, so the call sites do not change.
2. **`import kotlin.jvm.JvmField` / `JvmOverloads` / `JvmName`** added to 50 files. JVM code sees
   `kotlin.jvm.*` implicitly; wasmJs does not. The annotations themselves are untouched and still
   shape the JVM ABI exactly as upstream's do.
3. **`Locale.US` / `Locale.getDefault()` to `Locales.US` / `Locales.getDefault()`** (6 lines). An
   `expect class` cannot declare a companion that Java statics satisfy.
4. **`javaClass` to `this::class` / `::class.simpleName`** (10 lines, `A2uiSchema`,
   `A2uiException`, `A2uiComponentProperties`, `A2uiCoreComponentDefinitionSerializer`). On the JVM
   `KClass` equality, hash and `simpleName` equal `Class`'s, so `equals`/`hashCode`/messages match.
5. **`import java.lang.StringBuilder` to `kotlin.text.StringBuilder`** (`A2uiProperty`).
6. **Android resources** (`material3-a2ui`). `R.java` is dropped; `src/commonPort/kotlin/.../R.kt`
   declares an `R.string` object with upstream's **keys** and the default English values from
   upstream's `res/values/strings.xml`, plus a `stringResource(id, vararg args)` that substitutes
   `%n$s`. The four files that called `androidx.compose.ui.res.stringResource` import this one
   instead; every `R.string.*` call site is unchanged. Translations are not carried.
7. **`DateTimeInput`** (2 lines). `is24HourFormat(LocalContext.current)` becomes
   `is24HourFormat()` (no `Context` off Android), and `LocalLocale.current.platformLocale` (internal
   in CMP's common API) becomes `Locales.forLanguageTag(LocalLocale.current.toLanguageTag())`.
8. **`Slider`** (3 lines). CMP material3 1.13.0-alpha01 is AndroidX material3 1.5.0-alpha27, which
   predates `SliderState(trackRange = …)` and `Slider(state, onValueChange = …)`; the port passes
   `valueRange =` and sets `sliderState.onValueChange` instead. Same behaviour, older spelling.
9. **Build-level, not source**: `material3-a2ui` opts in to `ExperimentalMaterial3Api`
   (`DatePickerDialog` is still experimental in alpha27), and `a2ui-model` declares
   `kotlinx-coroutines-core` as `api` (its `A2uiMessageProcessor` exposes `StateFlow`; on Android it
   arrives through `androidx.core`).

The `-documentation.md` files and `compose-ui-testing` are not vendored.

## wasmJs status

Every module **compiles** for `wasmJs` and publishes a klib. What is not done:

- `PortRuntimeTest` compiles for wasm but is not executed here: the Kotlin browser test tooling
  fetches Karma from `codeload.github.com`, which this environment's proxy refuses. Adding
  `nodejs()` to `a2ui-port-runtime` would run it wherever that download works.
- The wasm `java.text` stand-ins are en-US and UTC only. `formatDate` with a caller's pattern
  supports `y M d H h m s S a E X Z` and quoted literals; other letters throw, which upstream maps to
  `A2uiRuntimeException`. `formatNumber`/`formatCurrency` are en-US. A browser renderer would route
  these through `Intl` instead.
- `identityHashCode` is `hashCode()`, and the concurrency types are unsynchronized: correct on the
  single-threaded browser, not a general-purpose implementation.
- No wasm render has been produced yet: that needs a browser host module, not part of this port.

## Published artifacts

```kotlin
repositories {
  maven("https://raw.githubusercontent.com/yschimke/a2ui-catalog-out/a2ui-cmp-maven/") {
    content { includeGroup("ee.schimke.a2uicmp") }
  }
}

dependencies {
  implementation("ee.schimke.a2uicmp:material3-a2ui:1.0.0-alpha01-cmp01")
}
```

[`publish-a2ui-cmp.yml`](../.github/workflows/publish-a2ui-cmp.yml) pushes that tree on `main` when
the pin or a vendored module changes, orphan-creating the branch the first time and skipping a
version that is already there. `./gradlew publishA2uiCmpToBuildDir` builds the exact tree CI pushes
under `build/a2ui-cmp-maven`; `./gradlew -q printA2uiCmpPortVersion` prints the version.

`:a2ui-desktop` is the proof it works without Robolectric: `DesktopRenderTest` parses a
createSurface / updateDataModel / updateComponents payload with the port's JSON reader, applies it
synchronously, draws it with the vendored Material catalog under Skiko, asserts on semantics and
pixels, and writes [`docs/evidence/cmp-desktop-render.png`](../docs/evidence/cmp-desktop-render.png).

The vendored source is Apache 2.0 licensed and keeps its Android Open Source Project headers; the
seam's files say they are not AndroidX code.
