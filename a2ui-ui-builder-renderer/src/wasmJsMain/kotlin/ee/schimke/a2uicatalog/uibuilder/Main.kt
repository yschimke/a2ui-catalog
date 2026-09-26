@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ee.schimke.a2uicatalog.uibuilder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ee.schimke.composeai.uibuilder.renderer.sdk.UiBuilderInspectionCollector
import ee.schimke.composeai.uibuilder.renderer.sdk.UiBuilderPixelBounds
import ee.schimke.composeai.uibuilder.renderer.sdk.UiBuilderSemanticActionController
import ee.schimke.composeai.uibuilder.renderer.sdk.startCatalogRenderer
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.jetbrains.skiko.InternalSkikoApi
import org.jetbrains.skiko.wasm.awaitSkiko

/**
 * The A2UI catalog's UI-builder renderer: the editor's sandboxed frame for a design pinned to this
 * catalog.
 *
 * Every render request is the whole design. It is lowered to the A2UI messages an agent would send
 * ([lowerDesign]), applied synchronously to a fresh message processor ([DesignSurface]), and drawn
 * by the vendored port's Material 3 basic catalog, so the canvas shows what an A2UI client draws
 * rather than a stand-in. Bounds for the editor's selection overlay come from each drawn component
 * ([inspectedCatalog]).
 */
@OptIn(InternalSkikoApi::class)
fun main() {
  val actions = UiBuilderSemanticActionController()
  awaitSkiko.then(
    onFulfilled = {
      loadRoboto(
        onLoaded = { family -> start(actions, family) },
        onFailed = { error("the renderer's fonts did not load: $it") },
      )
      null
    },
    onRejected = { error("Skiko initialization failed: $it") },
  )
}

private fun start(actions: UiBuilderSemanticActionController, family: FontFamily) {
  val typography = robotoTypography(family)
  startCatalogRenderer(actions) { document, surface, renderSessionId, onInspectionSnapshot ->
    val hostDensity = LocalDensity.current
    val density =
      Density(
        density = surface.density,
        fontScale =
          document.environment["fontScale"]
            ?.let { it as? JsonPrimitive }
            ?.contentOrNull
            ?.toFloatOrNull()
            ?.takeIf { it.isFinite() && it > 0f } ?: hostDensity.fontScale,
      )
    val layoutDirection =
      if ((document.environment["layoutDirection"] as? JsonPrimitive)?.contentOrNull == "rtl")
        LayoutDirection.Rtl
      else LayoutDirection.Ltr
    val isDark =
      (document.environment["uiMode"] as? JsonPrimitive)?.contentOrNull?.contains("night") == true
    val currentSnapshot = rememberUpdatedState(onInspectionSnapshot)
    val inspection =
      remember(document.id, document.revision, renderSessionId) {
        UiBuilderInspectionCollector(
          document = document,
          onSnapshot = { currentSnapshot.value(it) },
        )
      }
    val design = remember(document) { lowerDesign(document) }
    SideEffect { inspection.publishSnapshot() }

    CompositionLocalProvider(
      LocalDensity provides density,
      LocalLayoutDirection provides layoutDirection,
    ) {
      MaterialTheme(
        colorScheme = if (isDark) darkScheme else lightScheme,
        typography = typography,
      ) {
        Surface(
          Modifier.requiredSize(surface.widthDp.dp, surface.heightDp.dp).onGloballyPositioned {
            actions.install(
              emptyMap(),
              UiBuilderPixelBounds(0f, 0f, it.size.width.toFloat(), it.size.height.toFloat()),
            )
          }
        ) {
          when (design) {
            is LoweredDesign.Refused -> Refusal(design.reasons)
            is LoweredDesign.Messages -> DesignSurface(design, inspection)
          }
        }
      }
    }
  }
}

@Composable
private fun DesignSurface(
  design: LoweredDesign.Messages,
  inspection: UiBuilderInspectionCollector,
) {
  val host = remember(design) { SynchronousA2uiHost(inspectedCatalog) }
  DisposableEffect(host) { onDispose { host.close() } }
  val applied = remember(host) { runCatching { host.apply(design.messages) } }
  val surfaceModel = host.surface(design.surfaceId)
  if (applied.isFailure || surfaceModel == null) {
    Refusal(
      listOf(
        applied.exceptionOrNull()?.message
          ?: "the A2UI engine did not create surface ${design.surfaceId}"
      )
    )
    return
  }
  CompositionLocalProvider(
    LocalNodeBounds provides
      { designNodeId, coordinates ->
        val nodeId = design.designNodeIds[designNodeId] ?: designNodeId
        val origin = coordinates.positionInRoot()
        val unit = coordinates.localToRoot(Offset(1f, 1f)) - coordinates.localToRoot(Offset.Zero)
        runCatching {
          inspection.recordNodeBounds(
            nodeId,
            origin.x,
            origin.y,
            origin.x + coordinates.size.width * unit.x,
            origin.y + coordinates.size.height * unit.y,
          )
        }
      }
  ) {
    Box(Modifier.fillMaxSize()) { A2uiSurface(surfaceModel = surfaceModel, transitionSpec = null) }
  }
}

@Composable
private fun Refusal(reasons: List<String>) {
  Column(
    Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(16.dp)
  ) {
    Text(
      "This design cannot be sent as A2UI",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onErrorContainer,
    )
    reasons.forEach {
      Text(
        it,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier.padding(top = 8.dp),
      )
    }
  }
}
