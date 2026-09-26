@file:CatalogGroup(name = "Actions", section = "Input")

package ee.schimke.a2uicatalog.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ee.schimke.a2uicatalog.harness.A2uiSticker
import ee.schimke.a2uicatalog.harness.component
import ee.schimke.a2uicatalog.harness.event
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.CatalogVariant

// A2UI's Button has three `variant`s and the Material catalog draws each with a different Material
// function: `primary` is a filled `Button`, `default` an `OutlinedButton`, `borderless` a
// `TextButton`. That is one A2UI component with one argument, so the three are cells of `Button` —
// and because each is a different kit node, each cell names its own.

@Composable
private fun ButtonOf(variant: String, label: String = "Confirm", child: String = "Text") =
  A2uiSticker(
    listOf(
      component(
        "root",
        "Button",
        "child" to "content",
        "variant" to variant,
        "action" to event("confirm"),
      ),
      if (child == "Text") component("content", "Text", "text" to label)
      else component("content", "Icon", "name" to "send"),
    )
  )

@CatalogComponent(
  id = "Button",
  // M3 kit `Button` / Filled — `variant: primary` is a filled `Button`.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/57994:2324",
  caption = "Dispatches an action. `primary` is the filled button.",
)
@Preview
@Composable
fun ButtonSticker() = ButtonOf("primary")

@CatalogVariant(
  of = "Button",
  props = ["variant=default"],
  caption = "The default variant: an outlined button.",
  // M3 kit `Button` / Outlined.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/57994:2284",
)
@Preview
@Composable
fun ButtonDefaultSticker() = ButtonOf("default")

@CatalogVariant(
  of = "Button",
  props = ["variant=borderless"],
  caption = "A text button, for the least emphasis.",
  // M3 kit `Button` / Text.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/57994:2264",
)
@Preview
@Composable
fun ButtonBorderlessSticker() = ButtonOf("borderless")

@CatalogVariant(
  of = "Button",
  props = ["content=icon"],
  caption = "A button whose child is an Icon rather than Text.",
  // The kit's Button carries its icon BESIDE a label; an icon-only child is A2UI's composition, and
  // the kit's icon-only control is `IconButton`, a different component the catalog does not draw.
  noReference = "Icon-only child; the kit's Button always carries a label.",
)
@Preview
@Composable
fun ButtonIconSticker() = ButtonOf("primary", child = "Icon")
