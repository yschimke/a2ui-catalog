package ee.schimke.a2uicatalog.uibuilder

import androidx.a2ui.compose.runtime.A2uiComponentProperties
import androidx.a2ui.compose.runtime.A2uiComponentScope
import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.compose.runtime.A2uiProperty
import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.compose.ui.A2uiComponent
import androidx.a2ui.compose.ui.A2uiMessageProcessor
import androidx.a2ui.model.catalog.functions.A2uiLocaleProvider
import androidx.a2ui.model.processor.A2uiMessageProcessor
import androidx.a2ui.model.processor.A2uiSurfaceModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults
import androidx.compose.material3.a2ui.catalog.materialA2uiBasicCatalogV1
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * The property [lowerDesign] adds to every component: the A2UI id it was sent with. Declared on
 * each component of [inspectedCatalog], so the engine carries it like any other property.
 */
internal const val NODE_ID_PROPERTY = "uiBuilderNodeId"

private val nodeIdProperty =
  A2uiProperty.string(
    NODE_ID_PROPERTY,
    description = "The UI-builder canvas's own marker; never sent by an agent.",
  )

/** Where a drawn component reports its layout, by A2UI id. Nothing listens outside a render. */
internal val LocalNodeBounds =
  staticCompositionLocalOf<(String, LayoutCoordinates) -> Unit> { { _, _ -> } }

/**
 * The vendored Material 3 basic catalog, with media drawn as flat placeholders (the library leaves
 * loading media to the app, and a canvas frame must not reach the network), every component wrapped
 * to report its bounds.
 */
internal val inspectedCatalog: A2uiCatalog by lazy {
  val material =
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
  A2uiCatalog(
    catalogId = material.id,
    components = material.components.map(::InspectedComponent),
    functions = material.functions,
    themeSchema = material.themeSchema,
    isInline = material.isInline,
  )
}

/** [inner], unchanged, except that it reports where it was laid out to [LocalNodeBounds]. */
private class InspectedComponent(private val inner: A2uiComponent) : A2uiComponent {
  override val name: String = inner.name
  override val description: String = inner.description
  override val properties: List<A2uiProperty<*>> = inner.properties + nodeIdProperty

  @Composable
  override fun A2uiComponentScope.isReady(properties: A2uiComponentProperties): Boolean =
    with(inner) { isReady(properties) }

  @Composable
  override fun A2uiComponentScope.Content(properties: A2uiComponentProperties, modifier: Modifier) {
    val id = properties[nodeIdProperty]
    val report = LocalNodeBounds.current
    val inspected = if (id == null) modifier else modifier.onGloballyPositioned { report(id, it) }
    with(inner) { Content(properties, inspected) }
  }
}

@Composable
private fun MediaPlaceholder(modifier: Modifier) {
  Box(
    modifier
      .fillMaxWidth()
      .aspectRatio(16f / 9f)
      .background(MaterialTheme.colorScheme.surfaceVariant)
  )
}

/**
 * `:a2ui-desktop`'s host on Wasm: the engine's collector runs on [Dispatchers.Unconfined], started
 * undispatched, so every message has been applied by the time [apply] returns and the surface
 * exists in the same composition that asked for it.
 */
internal class SynchronousA2uiHost(catalog: A2uiCatalog) : AutoCloseable {
  private val processor: A2uiMessageProcessor = A2uiMessageProcessor(catalogs = listOf(catalog))
  private val parser = A2uiMessageParser()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

  init {
    scope.launch(start = CoroutineStart.UNDISPATCHED) { processor.collectMessages() }
  }

  fun apply(messages: List<String>) {
    for (message in messages) processor.processMessage(parser.parse(message))
  }

  fun surface(surfaceId: String): A2uiSurfaceModel? =
    processor.activeSurfaces.value.firstOrNull { it.id == surfaceId }

  override fun close() = scope.cancel()
}
