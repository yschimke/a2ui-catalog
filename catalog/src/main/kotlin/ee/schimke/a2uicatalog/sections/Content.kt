@file:CatalogGroup(name = "Content", section = "Content")

package ee.schimke.a2uicatalog.sections

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ee.schimke.a2uicatalog.harness.A2uiSticker
import ee.schimke.a2uicatalog.harness.component
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.CatalogVariant

// Text's `variant` is one axis of one component — the catalog maps h1…h5, caption and body onto
// Material type roles — so every role is a cell of `Text`, never a card of its own.

@Composable
private fun TextOf(variant: String, text: String) =
  A2uiSticker(listOf(component("root", "Text", "text" to text, "variant" to variant)))

@CatalogComponent(
  id = "Text",
  noReference =
    "The M3 kit publishes type STYLES, not a Text component; the roles each variant maps to are " +
      "the Material type scale.",
  caption = "A run of text in one of seven roles. `body` by default.",
)
@Preview
@Composable
fun TextSticker() = TextOf("body", "Agents describe UI; the client draws it.")

@CatalogVariant(of = "Text", props = ["variant=h1"], caption = "The largest heading.")
@Preview
@Composable
fun TextH1Sticker() = TextOf("h1", "Heading 1")

@CatalogVariant(of = "Text", props = ["variant=h2"], caption = "Second-level heading.")
@Preview
@Composable
fun TextH2Sticker() = TextOf("h2", "Heading 2")

@CatalogVariant(of = "Text", props = ["variant=h3"], caption = "Third-level heading.")
@Preview
@Composable
fun TextH3Sticker() = TextOf("h3", "Heading 3")

@CatalogVariant(of = "Text", props = ["variant=h4"], caption = "Fourth-level heading.")
@Preview
@Composable
fun TextH4Sticker() = TextOf("h4", "Heading 4")

@CatalogVariant(of = "Text", props = ["variant=h5"], caption = "Fifth-level heading.")
@Preview
@Composable
fun TextH5Sticker() = TextOf("h5", "Heading 5")

@CatalogVariant(of = "Text", props = ["variant=caption"], caption = "Secondary, small print.")
@Preview
@Composable
fun TextCaptionSticker() = TextOf("caption", "Updated two minutes ago")

@Composable
private fun IconOf(name: Any) = A2uiSticker(listOf(component("root", "Icon", "name" to name)))

@CatalogComponent(
  id = "Icon",
  noReference =
    "The glyph set is the catalog's own (`material3-a2ui` bundles its 60 icons); the M3 kit's " +
      "Icons page is a glyph library, not a component to compare against.",
  caption = "One of the catalog's named glyphs.",
)
@Preview
@Composable
fun IconSticker() = IconOf("favorite")

@CatalogVariant(of = "Icon", props = ["name=settings"], caption = "Another built-in glyph.")
@Preview
@Composable
fun IconSettingsSticker() = IconOf("settings")

@CatalogVariant(
  of = "Icon",
  props = ["name=svgPath"],
  caption = "A glyph the agent supplies as an SVG path instead of naming a built-in one.",
)
@Preview
@Composable
fun IconSvgPathSticker() =
  IconOf(mapOf("svgPath" to "M12 2 15 9 22 9 16.5 14 18.5 21 12 17 5.5 21 7.5 14 2 9 9 9Z"))

@CatalogComponent(
  id = "Divider",
  // M3 kit `Divider` / Horizontal — the catalog draws a `HorizontalDivider` at its defaults.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/51816:5860",
  caption = "A hairline between groups of content.",
)
@Preview
@Composable
fun DividerSticker() =
  A2uiSticker(
    listOf(
      component(
        "root",
        "Column",
        "children" to listOf("above", "rule", "below"),
        "align" to "stretch",
      ),
      component("above", "Text", "text" to "Inbox", "variant" to "body"),
      component("rule", "Divider", "axis" to "horizontal"),
      component("below", "Text", "text" to "Archive", "variant" to "body"),
    )
  )

@CatalogVariant(
  of = "Divider",
  props = ["axis=vertical"],
  caption = "A vertical rule between two items in a row.",
  // A vertical divider only has height inside something that gives it one, so this cell draws it
  // in a row between two labels. That is a composition the kit's bare 1dp `Divider/Vertical` node
  // cannot be compared against, hence no reference.
  noReference = "Drawn between two labels in a row; the kit node is the bare rule.",
)
@Preview
@Composable
fun DividerVerticalSticker() =
  A2uiSticker(
    listOf(
      component("root", "Row", "children" to listOf("left", "rule", "right"), "align" to "stretch"),
      component("left", "Text", "text" to "Inbox", "variant" to "body"),
      component("rule", "Divider", "axis" to "vertical"),
      component("right", "Text", "text" to "Archive", "variant" to "body"),
    ),
    // A vertical divider fills the height it is given; sized to the row's intrinsic height it is
    // exactly as tall as the labels beside it.
    modifier = Modifier.height(IntrinsicSize.Min),
  )
