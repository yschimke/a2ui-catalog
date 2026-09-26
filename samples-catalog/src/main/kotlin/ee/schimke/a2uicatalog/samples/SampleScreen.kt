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
import ee.schimke.a2uicatalog.harness.A2uiDocument
import ee.schimke.a2uicatalog.harness.StickerCatalog
import ee.schimke.a2uicatalog.harness.rememberStickerHost
import ee.schimke.composeai.overrides.previewOverrideString
import org.json.JSONArray
import org.json.JSONObject

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
      var sent by remember { mutableStateOf<String?>(null) }
      val sink: PayloadSink = { components, data ->
        sent = payloadDocument(components, data)
        surface = host.show(components, data)
      }
      // The payload the sample just sent, published as the A2UI playground's `document` knob with
      // that payload as its default: the viewer shows it as editable text, and
      // compose-preview-server's
      // playground opens any preview that declares the knob. Declared once the sample has sent
      // something (its payload arrives from a LaunchedEffect), and re-declared as it changes; the
      // last declaration is the one recorded. Only an edited document replaces the sample's own.
      val document = sent?.let { previewOverrideString(DOCUMENT_KNOB, it) }
      // An edited document is applied to the same host and drawn in the same card, under the same
      // theme and framing, so an edit changes what the payload says and nothing else.
      val edited =
        document
          ?.takeIf { it != sent }
          ?.let { source -> remember(host, source) { A2uiDocument.parse(source) } }
      val editedSurface = edited?.let { remember(host, it) { host.show(it) } }
      Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        PreviewCard(
          component = component,
          surface = if (edited != null) editedSurface else surface,
          errors =
            edited?.errors?.ifEmpty {
              if (editedSurface == null) listOf("The edited document created no surface.")
              else emptyList()
            } ?: emptyList(),
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
private fun PreviewCard(
  component: UiComponent,
  surface: A2uiSurfaceModel?,
  errors: List<String>,
  modifier: Modifier,
) {
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
      if (errors.isNotEmpty()) {
        Text(
          errors.joinToString("\n"),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
        )
      } else if (surface != null) {
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

/** The knob the A2UI playground edits; the same key `:catalog`'s playground preview declares. */
const val DOCUMENT_KNOB = "document"

private const val SAMPLE_SURFACE_ID = "sample"

/**
 * [components] and [data] as the A2UI v0.9 messages an agent sends to draw them, one per line (JSON
 * Lines): `createSurface` on the basic catalog, `updateDataModel` when there is data, then
 * `updateComponents`. Not the demo's own `formatA2uiJson`, which folds the last two into one object
 * for its JSON sheet and so is not a message stream the playground can parse.
 */
internal fun payloadDocument(
  components: List<A2uiComponentPayload>,
  data: Map<String, Any?>,
): String {
  // org.json escapes every `/` as `\/`: legal JSON, but the knob is text a person reads and edits.
  // Every slash it writes is escaped, so undoing the escape cannot misread an escaped backslash.
  fun envelope(kind: String, body: JSONObject) =
    JSONObject().put("version", "v0.9").put(kind, body).toString().replace("\\/", "/")
  return buildList {
      add(
        envelope(
          "createSurface",
          JSONObject().put("surfaceId", SAMPLE_SURFACE_ID).put("catalogId", StickerCatalog.id),
        )
      )
      if (data.isNotEmpty()) {
        add(
          envelope(
            "updateDataModel",
            JSONObject()
              .put("surfaceId", SAMPLE_SURFACE_ID)
              .put("path", "/")
              .put("value", jsonValue(data)),
          )
        )
      }
      add(
        envelope(
          "updateComponents",
          JSONObject()
            .put("surfaceId", SAMPLE_SURFACE_ID)
            .put(
              "components",
              JSONArray(
                components.map { component ->
                  JSONObject().put("id", component.id).put("component", component.type).also {
                    for ((key, value) in component.properties) it.put(key, jsonValue(value))
                  }
                }
              ),
            ),
        )
      )
    }
    .joinToString("\n")
}

private fun jsonValue(value: Any?): Any? =
  when (value) {
    null -> JSONObject.NULL
    is Map<*, *> ->
      JSONObject().also { obj -> value.forEach { (k, v) -> obj.put(k.toString(), jsonValue(v)) } }
    is List<*> -> JSONArray(value.map(::jsonValue))
    else -> value
  }
