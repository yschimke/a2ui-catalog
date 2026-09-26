package ee.schimke.a2uicatalog.harness

import androidx.a2ui.model.protocol.A2uiComponentPayload
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * One A2UI component tree, drawn by the Material 3 basic catalog.
 *
 * [components] is exactly what an agent would stream in an `updateComponents` message — the root
 * must have the id `root` — and [data] is the surface's data model, which dynamic bindings
 * (`{"path": "/…"}`) read from. Nothing about the picture is decided here: `A2uiSurface` draws it,
 * with `transitionSpec = null` because a still capture of a fade is a capture of half a fade.
 *
 * A payload the engine rejects draws the library's own error fallback rather than nothing, so a
 * malformed sticker is visible on the sheet instead of silently blank.
 */
@Composable
fun A2uiSticker(
  components: List<A2uiComponentPayload>,
  data: Map<String, Any?> = emptyMap(),
  modifier: Modifier = Modifier,
) {
  StickerTheme {
    val host = rememberStickerHost()
    val surface = remember(host, components, data) { host.show(components, data) }
    if (surface != null) {
      A2uiSurface(surfaceModel = surface, modifier = modifier, transitionSpec = null)
    } else {
      Surface(color = MaterialTheme.colorScheme.errorContainer) {
        Text("The engine created no surface for this payload.", Modifier.padding(16.dp))
      }
    }
  }
}

/**
 * The stock Material 3 theme on a surface-coloured ground.
 *
 * Stock, deliberately: the catalog is what `material3-a2ui` draws for an app that has not themed
 * it, and the M3 kit it is compared against is drawn in the baseline scheme. `Surface` supplies the
 * ground and the content colour a real host screen would; without it text draws black on a
 * transparent capture.
 */
@Composable
fun StickerTheme(content: @Composable () -> Unit) {
  MaterialTheme {
    Surface { Box(Modifier.padding(16.dp).widthIn(max = StickerWidth)) { content() } }
  }
}

/** `A2uiComponentPayload(id, type, mapOf(...))`, spelled the way the protocol's JSON reads. */
fun component(id: String, type: String, vararg properties: Pair<String, Any?>) =
  A2uiComponentPayload(id = id, type = type, properties = mapOf(*properties))

/** A data binding: `{"path": "/…"}`. */
fun bind(path: String): Map<String, Any?> = mapOf("path" to path)

/** A button action that dispatches the named event. */
fun event(name: String): Map<String, Any?> = mapOf("event" to mapOf("name" to name))

/**
 * The widest a sticker draws: a compact phone column less its margins. A2UI components that fill (a
 * stretched Column, a text field, a header image) stop here instead of filling the capture sandbox;
 * components that measure themselves are unaffected.
 */
val StickerWidth = 360.dp
