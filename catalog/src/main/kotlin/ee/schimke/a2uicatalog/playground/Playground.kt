package ee.schimke.a2uicatalog.playground

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ee.schimke.a2uicatalog.harness.A2uiDocumentView
import ee.schimke.composeai.overrides.previewOverrideString

/**
 * The A2UI playground: renders whatever A2UI document the `document` knob holds.
 *
 * This is the one preview here that is not a sticker. It exists so an A2UI document can be rendered
 * by the real `material3-a2ui` catalog WITHOUT a build: preview.coo.ee serves this catalog's live
 * bundle, and a live render of this preview with `document` overridden is a render of that document
 * — from the playground page, from the render API, or from `compose-preview a2ui render`.
 *
 * The document may be JSON Lines of A2UI v0.9 messages, a JSON array of them, or the
 * `{"components": [...]}` shorthand (see `A2uiDocument`). Messages the parser rejects are listed
 * beneath the render rather than failing it.
 *
 * It is deliberately NOT a `@CatalogComponent`: it is not a component of the basic catalog, and the
 * inventory (`CatalogInventoryTest`) is exactly the library's eighteen.
 */
@Preview(name = "A2UI document", widthDp = 412)
@Composable
fun A2uiDocumentPreview() = A2uiDocumentView(previewOverrideString(DOCUMENT_KNOB, DEFAULT_DOCUMENT))

/** The knob a caller overrides to render its own document. */
const val DOCUMENT_KNOB = "document"

/** What the playground shows before anything is pasted: a card using most of the catalog. */
val DEFAULT_DOCUMENT: String =
  """
  {"version":"v0.9","createSurface":{"surfaceId":"playground","catalogId":"https://a2ui.org/specification/v0_9/catalogs/basic/catalog.json"}}
  {"version":"v0.9","updateDataModel":{"surfaceId":"playground","path":"/","value":{"name":"Ada Lovelace","guests":2,"email":true}}}
  {"version":"v0.9","updateComponents":{"surfaceId":"playground","components":[{"id":"root","component":"Card","child":"body"},{"id":"body","component":"Column","align":"stretch","children":["title","subtitle","name","guests","email","actions"]},{"id":"title","component":"Text","text":"Weekend in Lisbon","variant":"h3"},{"id":"subtitle","component":"Text","text":"Two nights, flights included","variant":"caption"},{"id":"name","component":"TextField","label":"Full name","value":{"path":"/name"}},{"id":"guests","component":"Slider","label":"Guests","min":1,"max":6,"value":{"path":"/guests"}},{"id":"email","component":"CheckBox","label":"Email me a receipt","value":{"path":"/email"}},{"id":"actions","component":"Row","justify":"end","children":["book"]},{"id":"book","component":"Button","variant":"primary","child":"book_label","action":{"event":{"name":"book"}}},{"id":"book_label","component":"Text","text":"Book"}]}}
  """
    .trimIndent()
