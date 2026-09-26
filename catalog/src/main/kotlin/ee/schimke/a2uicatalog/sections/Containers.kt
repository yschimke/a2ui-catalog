@file:CatalogGroup(name = "Containers", section = "Layout")

package ee.schimke.a2uicatalog.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ee.schimke.a2uicatalog.harness.A2uiSticker
import ee.schimke.a2uicatalog.harness.component
import ee.schimke.a2uicatalog.harness.event
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup

// The Material catalog draws `Card` as an `OutlinedCard` and `Tabs` as a `PrimaryTabRow`, so both
// name the M3 kit node for that exact variant. `Modal` is a trigger plus a dialog that opens when
// the
// trigger is pressed; at rest — which is all a still capture shows — it is only the trigger.

@CatalogComponent(
  id = "Card",
  // M3 kit `Card` / Outlined — `material3-a2ui` draws every A2UI Card as an `OutlinedCard`.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/52346:27574",
  caption =
    "A container for one child, here the kit card's full set of slots. Drawn as an outlined card.",
  related = ["a2ui-samples"],
)
@Preview
@Composable
fun CardSticker() =
  A2uiSticker(
    listOf(
      component("root", "Card", "child" to "content"),
      component(
        "content",
        "Column",
        "children" to listOf("header", "media", "headline", "body", "actions"),
        "align" to "stretch",
      ),
      // The kit card's slots, each an A2UI component: a header row (avatar, header and subhead,
      // overflow icon), a media image, a title and subtitle, supporting text, and two actions.
      component(
        "header",
        "Row",
        "children" to listOf("avatar", "header_text", "more"),
        "align" to "center",
      ),
      component(
        "avatar",
        "Image",
        "url" to "https://example.com/a2ui/avatar.png",
        "variant" to "avatar",
      ),
      component(
        "header_text",
        "Column",
        "children" to listOf("header_title", "subhead"),
        "weight" to 1,
      ),
      component("header_title", "Text", "text" to "Lisbon trip", "variant" to "h5"),
      component("subhead", "Text", "text" to "Shared by Ada", "variant" to "caption"),
      component("more", "Icon", "name" to "moreVert"),
      component(
        "media",
        "Image",
        "url" to "https://example.com/a2ui/lisbon.jpg",
        "variant" to "header",
      ),
      component("headline", "Column", "children" to listOf("title", "subtitle")),
      component("title", "Text", "text" to "Weekend in Lisbon", "variant" to "h4"),
      component(
        "subtitle",
        "Text",
        "text" to "Two nights, flights included",
        "variant" to "caption",
      ),
      component(
        "body",
        "Text",
        "text" to "Trams, pastéis de nata and a sunset over the Tagus.",
        "variant" to "body",
      ),
      component("actions", "Row", "children" to listOf("secondary", "primary"), "justify" to "end"),
      component(
        "secondary",
        "Button",
        "child" to "secondary_text",
        "variant" to "default",
        "action" to event("share"),
      ),
      component("secondary_text", "Text", "text" to "Share"),
      component(
        "primary",
        "Button",
        "child" to "primary_text",
        "variant" to "primary",
        "action" to event("book"),
      ),
      component("primary_text", "Text", "text" to "Book"),
    )
  )

@CatalogComponent(
  id = "Tabs",
  // M3 kit `Tabs` / Primary — the Material catalog draws a `PrimaryTabRow`.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/54563:40116",
  caption = "Titled tabs over one child each. The first tab is selected.",
  related = ["a2ui-samples"],
)
@Preview
@Composable
fun TabsSticker() =
  A2uiSticker(
    listOf(
      component(
        "root",
        "Tabs",
        "tabs" to
          listOf("Flights", "Hotels", "Cars").mapIndexed { i, title ->
            mapOf("title" to title, "child" to "tab_$i")
          },
      )
    ) +
      listOf(
          "Three direct flights today.",
          "Twelve hotels near the centre.",
          "Pick-up at arrivals.",
        )
        .mapIndexed { i, text -> component("tab_$i", "Text", "text" to text, "variant" to "body") }
  )

@CatalogComponent(
  id = "Modal",
  noReference =
    "At rest a Modal draws only its trigger; the dialog it opens (a BasicAlertDialog) is not " +
      "shown until the trigger is pressed, so there is no resting picture to compare to the kit.",
  caption = "A trigger that opens its content in a dialog.",
  related = ["a2ui-samples"],
)
@Preview
@Composable
fun ModalSticker() =
  A2uiSticker(
    listOf(
      component("root", "Modal", "trigger" to "open", "content" to "dialog"),
      component(
        "open",
        "Button",
        "child" to "open_text",
        "variant" to "primary",
        "action" to event("open_modal"),
      ),
      component("open_text", "Text", "text" to "Delete project…"),
      component("dialog", "Column", "children" to listOf("dialog_title", "dialog_body")),
      component("dialog_title", "Text", "text" to "Delete project?", "variant" to "h3"),
      component("dialog_body", "Text", "text" to "This cannot be undone.", "variant" to "body"),
    )
  )
