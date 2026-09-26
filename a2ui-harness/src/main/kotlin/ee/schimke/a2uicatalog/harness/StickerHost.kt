package ee.schimke.a2uicatalog.harness

import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.model.processor.A2uiMessageProcessor
import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.a2ui.model.protocol.A2uiComponentPayload
import androidx.a2ui.model.protocol.A2uiCreateSurfaceMessage
import androidx.a2ui.model.protocol.A2uiUpdateComponentsMessage
import androidx.a2ui.model.protocol.A2uiUpdateDataModelMessage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * A message processor whose messages are applied by the time [show] returns.
 *
 * The engine queues every message on an unlimited channel and applies it from `collectMessages()`,
 * launching one actor per surface. Collected on [Dispatchers.Unconfined], a `trySend` resumes the
 * collector in the caller's own frame, and every coroutine it launches runs on the same thread's
 * event loop before control comes back — so after `show(...)` the surface exists and its components
 * are registered, with no frame, thread hop or timing assumption between the payload and the
 * picture. `StickerHostTest` holds it to that.
 *
 * The collector is cancelled only when the host leaves the composition, because cancelling it
 * DISPOSES every surface it created (see `A2uiCoreMessageProcessor.collectMessages`): a host that
 * pre-processed and then stopped would hand back a dead surface.
 */
class StickerHost(val catalog: A2uiCatalog) : RememberObserver {
  val processor: A2uiMessageProcessor = A2uiMessageProcessor(catalogs = listOf(catalog))

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

  init {
    scope.launch(start = CoroutineStart.UNDISPATCHED) { processor.collectMessages() }
  }

  /**
   * Creates (or replaces the contents of) [surfaceId]: the data model first, then the components,
   * the order an agent streams them in. Returns the surface, or null if the engine rejected it —
   * which the engine reports as an outbound error rather than an exception.
   */
  fun show(
    components: List<A2uiComponentPayload>,
    data: Map<String, Any?> = emptyMap(),
    surfaceId: String = DEFAULT_SURFACE_ID,
  ): A2uiSurfaceModel? {
    processor.processMessage(A2uiCreateSurfaceMessage(surfaceId, catalog.id))
    if (data.isNotEmpty()) {
      processor.processMessage(A2uiUpdateDataModelMessage(surfaceId, "/", data))
    }
    processor.processMessage(A2uiUpdateComponentsMessage(surfaceId, components))
    return processor.activeSurfaces.value.firstOrNull { it.id == surfaceId }
  }

  fun close() = scope.cancel()

  override fun onRemembered() {}

  override fun onForgotten() = close()

  override fun onAbandoned() = close()

  companion object {
    const val DEFAULT_SURFACE_ID: String = "sticker"
  }
}

/** A [StickerHost] tied to the composition, on the catalog every sticker here draws with. */
@Composable
fun rememberStickerHost(catalog: A2uiCatalog = StickerCatalog): StickerHost =
  remember(catalog) { StickerHost(catalog) }
