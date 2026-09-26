@file:CatalogGroup(name = "Layout", section = "Layout")

package ee.schimke.a2uicatalog.sections

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ee.schimke.a2uicatalog.harness.A2uiSticker
import ee.schimke.a2uicatalog.harness.component
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.CatalogVariant

// Row, Column and List are the basic catalog's three arrangement components. None of them is a
// Material component — the Material catalog draws them with foundation's `Row`, `Column` and the
// lazy lists — so the M3 kit has nothing to compare them against, and each enters with
// `noReference`.
//
// A Row sizes to its children. Its `justify` only shows when the row is given width, and in A2UI
// the way a row is given width is its parent: a Column with `align: stretch` stretches every child
// across itself. The justify stickers therefore put the row in exactly that column, which is how an
// agent would have to write it too.

/** Three labelled chips, the children every arrangement sticker lays out. */
private fun items(prefix: String) =
  listOf("One", "Two", "Three").mapIndexed { i, label ->
    listOf(
      component("${prefix}_$i", "Card", "child" to "${prefix}_${i}_text"),
      component("${prefix}_${i}_text", "Text", "text" to label, "variant" to "body"),
    )
  }

private fun itemIds(prefix: String) = List(3) { "${prefix}_$it" }

@Composable
private fun StretchedRow(justify: String) =
  A2uiSticker(
    listOf(
      component("root", "Column", "children" to listOf("row"), "align" to "stretch"),
      component(
        "row",
        "Row",
        "children" to itemIds("item"),
        "justify" to justify,
        "align" to "center",
      ),
    ) + items("item").flatten()
  )

@CatalogComponent(
  id = "Row",
  noReference =
    "A2UI layout primitive drawn with foundation's Row; the M3 kit publishes no layout component.",
  caption = "Children side by side. `justify` spreads them along the row, `align` across it.",
)
@Preview
@Composable
fun RowSticker() = StretchedRow(justify = "start")

@CatalogVariant(
  of = "Row",
  props = ["justify=spaceBetween"],
  caption = "Space between the children.",
)
@Preview
@Composable
fun RowSpaceBetweenSticker() = StretchedRow(justify = "spaceBetween")

@CatalogVariant(of = "Row", props = ["justify=center"], caption = "Children centred along the row.")
@Preview
@Composable
fun RowCenterSticker() = StretchedRow(justify = "center")

@CatalogVariant(of = "Row", props = ["justify=end"], caption = "Children packed at the end.")
@Preview
@Composable
fun RowEndSticker() = StretchedRow(justify = "end")

@Composable
private fun ColumnOf(align: String) =
  A2uiSticker(
    listOf(
      component(
        "root",
        "Column",
        "children" to listOf("title", "body", "action"),
        "align" to align,
      ),
      component("title", "Text", "text" to "Weekend in Lisbon", "variant" to "h3"),
      component("body", "Text", "text" to "Two nights, flights included.", "variant" to "body"),
      component(
        "action",
        "Button",
        "child" to "action_text",
        "variant" to "primary",
        "action" to mapOf("event" to mapOf("name" to "book")),
      ),
      component("action_text", "Text", "text" to "Book"),
    )
  )

@CatalogComponent(
  id = "Column",
  noReference =
    "A2UI layout primitive drawn with foundation's Column; the M3 kit publishes no layout component.",
  caption = "Children stacked vertically. `align: start` keeps each child at its own width.",
)
@Preview
@Composable
fun ColumnSticker() = ColumnOf(align = "start")

@CatalogVariant(
  of = "Column",
  props = ["align=center"],
  caption = "Children centred across the column.",
)
@Preview
@Composable
fun ColumnCenterSticker() = ColumnOf(align = "center")

@CatalogVariant(
  of = "Column",
  props = ["align=stretch"],
  caption = "Every child stretched to the column's width — the button included.",
)
@Preview
@Composable
fun ColumnStretchSticker() = ColumnOf(align = "stretch")

@Composable
private fun ListOf(direction: String) =
  A2uiSticker(
    listOf(component("root", "List", "children" to itemIds("item"), "direction" to direction)) +
      items("item").flatten(),
    // A horizontal list stretches its items to the height it is given, and the capture sandbox is
    // a whole screen tall. A lazy list cannot be measured intrinsically, so the frame is a fixed
    // strip the height of one item row, which is the picture a horizontal list is drawn at in a
    // real screen.
    modifier = if (direction == "horizontal") Modifier.height(72.dp) else Modifier,
  )

@CatalogComponent(
  id = "List",
  noReference =
    "A2UI scrolling container drawn with foundation's lazy lists; the M3 kit's `List` is a list " +
      "ITEM with leading and trailing slots, which A2UI does not publish.",
  caption = "A scrolling run of children. Vertical by default.",
)
@Preview
@Composable
fun ListSticker() = ListOf(direction = "vertical")

@CatalogVariant(
  of = "List",
  props = ["direction=horizontal"],
  caption = "The same children in a horizontally scrolling row.",
)
@Preview
@Composable
fun ListHorizontalSticker() = ListOf(direction = "horizontal")
