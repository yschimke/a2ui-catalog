package ee.schimke.a2uicatalog.desktop

import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.model.catalog.functions.A2uiLocaleProvider
import androidx.a2ui.model.processor.A2uiMessageProcessor
import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults
import androidx.compose.material3.a2ui.catalog.materialA2uiBasicCatalogV1
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The Material 3 basic catalog from the vendored CMP port, with media drawn as flat placeholders
 * (the library leaves media loading to the app; nothing here may touch the network).
 */
val DesktopCatalog: A2uiCatalog =
  materialA2uiBasicCatalogV1(
    image =
      MaterialA2uiBasicCatalogV1Defaults.image { _, _, _, modifier, _ ->
        MediaPlaceholder(modifier)
      },
    video =
      MaterialA2uiBasicCatalogV1Defaults.video { _, modifier, _ -> MediaPlaceholder(modifier) },
    audioPlayer =
      MaterialA2uiBasicCatalogV1Defaults.audioPlayer { _, _, modifier, _ ->
        MediaPlaceholder(modifier)
      },
    urlOpener = {},
    messageFormatter = { pattern, _, _ -> pattern },
    localeProvider = A2uiLocaleProvider.Default,
  )

@androidx.compose.runtime.Composable
private fun MediaPlaceholder(modifier: Modifier) {
  Box(modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color(0xFFD0D0D0)))
}

/**
 * `:a2ui-harness`'s `StickerHost`, against the port: the engine's collector runs on
 * [Dispatchers.Unconfined] (started undispatched), so a message has been applied by the time
 * [processJson] returns and the surface exists in the first composition — no frame, thread hop or
 * `Dispatchers.Main` between the payload and the picture.
 */
class DesktopA2uiHost(val catalog: A2uiCatalog = DesktopCatalog) : AutoCloseable {
  val processor: A2uiMessageProcessor = A2uiMessageProcessor(catalogs = listOf(catalog))

  private val parser = A2uiMessageParser()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

  init {
    scope.launch(start = CoroutineStart.UNDISPATCHED) { processor.collectMessages() }
  }

  /** Parses each JSON message with the port's JSON reader and applies it, in order. */
  fun processJson(vararg messages: String) {
    for (message in messages) processor.processMessage(parser.parse(message))
  }

  fun surface(surfaceId: String): A2uiSurfaceModel? =
    processor.activeSurfaces.value.firstOrNull { it.id == surfaceId }

  override fun close() = scope.cancel()
}
