package ee.schimke.a2uicatalog.harness

import androidx.a2ui.engine.model.A2uiCoreSurfaceModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * The property every sticker depends on: a payload is APPLIED by the time `show` returns. If the
 * engine ever started deferring work past the caller's frame, stickers would capture an empty
 * surface — and a render task still exits 0 on an empty picture, so this is where it has to fail.
 */
class StickerHostTest {

  @Test
  fun `show creates the surface and registers every component synchronously`() {
    val host = StickerHost(StickerCatalog)
    try {
      val surface =
        host.show(
          listOf(
            component("root", "Column", "children" to listOf("title", "body")),
            component("title", "Text", "text" to "Title", "variant" to "h3"),
            component("body", "Text", "text" to bind("/body")),
          ),
          data = mapOf("body" to "Body"),
        )
      assertNotNull(surface, "the surface should exist as soon as show() returns")
      assertEquals(StickerHost.DEFAULT_SURFACE_ID, surface.id)
      val core = surface as A2uiCoreSurfaceModel
      assertEquals(StickerCatalog.id, core.catalog.id)
    } finally {
      host.close()
    }
  }

  @Test
  fun `the catalog is the A2UI basic catalog v1 with all eighteen components`() {
    val names =
      StickerCatalog.components.let { c -> BasicCatalogComponents.filter { c[it] != null } }
    assertEquals(BasicCatalogComponents, names)
  }
}
