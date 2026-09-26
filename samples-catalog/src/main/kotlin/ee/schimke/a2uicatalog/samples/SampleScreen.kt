package ee.schimke.a2uicatalog.samples

import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.a2ui.model.protocol.A2uiComponentPayload
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.material3.integration.a2ui.DemoTheme
import androidx.compose.material3.integration.a2ui.model.UiComponent
import androidx.compose.material3.integration.a2ui.ui.drawDottedPattern
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ee.schimke.a2uicatalog.harness.StickerCatalog
import ee.schimke.a2uicatalog.harness.rememberStickerHost

/** The callback every vendored `<Component>Sample` takes: the payload its controls describe. */
typealias PayloadSink = (List<A2uiComponentPayload>, Map<String, Any?>) -> Unit

/**
 * The AndroidX demo's component screen — the A2UI preview in a dotted card above the sample's
 * controls — drawn on the synchronous `:a2ui-harness` host.
 *
 * This is `ComponentDetailScreen`'s stacked (compact-width) layout with three things taken out,
 * each because a still capture cannot stand behind it: the top app bar and JSON sheet (navigation),
 * the `Dispatchers.Default` message collector (a race against the capture), and the Coil image
 * loader (the network). The per-component surface modifiers are upstream's own, copied from
 * `ComponentPreviewCard`, so a component is framed in the card exactly as the demo frames it.
 */
@Composable
fun SampleScreen(component: UiComponent, controls: @Composable (PayloadSink) -> Unit) {
  DemoTheme {
    Surface(modifier = Modifier.fillMaxSize()) {
      val host = rememberStickerHost(StickerCatalog)
      var surface by remember { mutableStateOf<A2uiSurfaceModel?>(null) }
      val sink: PayloadSink = { components, data -> surface = host.show(components, data) }
      Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        PreviewCard(
          component = component,
          surface = surface,
          modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().height(240.dp),
        )
        Text(
          text = component.displayName,
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(vertical = 8.dp),
        )
        controls(sink)
      }
    }
  }
}

@Composable
private fun PreviewCard(component: UiComponent, surface: A2uiSurfaceModel?, modifier: Modifier) {
  Card(
    modifier = modifier,
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.elevatedCardColors(),
    elevation = CardDefaults.elevatedCardElevation(),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .drawDottedPattern(
            dotColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            spacing = 16.dp,
            dotRadius = 1.25.dp,
          )
          .padding(16.dp),
      contentAlignment = Alignment.Center,
    ) {
      if (surface != null) {
        A2uiSurface(
          surfaceModel = surface,
          modifier = surfaceModifier(component),
          transitionSpec = null,
        )
      }
    }
  }
}

/** Upstream's `ComponentPreviewCard` framing, per component. */
private fun surfaceModifier(component: UiComponent): Modifier =
  when (component) {
    UiComponent.ROW,
    UiComponent.TABS,
    UiComponent.SLIDER,
    UiComponent.DATE_TIME_INPUT,
    UiComponent.CHOICE_PICKER ->
      Modifier.fillMaxWidth().wrapContentHeight(Alignment.CenterVertically)
    UiComponent.COLUMN,
    UiComponent.LIST,
    UiComponent.DIVIDER -> Modifier.fillMaxSize()
    else -> Modifier.wrapContentSize(Alignment.Center)
  }
