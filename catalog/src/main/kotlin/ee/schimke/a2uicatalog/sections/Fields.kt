@file:CatalogGroup(name = "Fields", section = "Input")

package ee.schimke.a2uicatalog.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ee.schimke.a2uicatalog.harness.A2uiSticker
import ee.schimke.a2uicatalog.harness.bind
import ee.schimke.a2uicatalog.harness.component
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.CatalogVariant

// Every input binds its value to the surface's data model (`{"path": "/…"}`), the two-way binding
// an
// agent uses to read what the user entered. Each sticker seeds that model, so the picture is the
// field holding a value rather than an empty one — the resting state a reader most needs to see.

@Composable
private fun TextFieldOf(variant: String, value: String, label: String = "Full name") =
  A2uiSticker(
    listOf(
      component(
        "root",
        "TextField",
        "label" to label,
        "value" to bind("/value"),
        "variant" to variant,
      )
    ),
    data = mapOf("value" to value),
  )

@CatalogComponent(
  id = "TextField",
  // M3 kit `Text field` / Outlined — the catalog draws every variant with `OutlinedTextField`.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/52798:24397",
  caption = "A labelled text input bound to the data model. `shortText` by default.",
)
@Preview
@Composable
fun TextFieldSticker() = TextFieldOf("shortText", "Ada Lovelace")

@CatalogVariant(
  of = "TextField",
  props = ["variant=longText"],
  caption = "Multi-line entry.",
)
@Preview
@Composable
fun TextFieldLongTextSticker() =
  TextFieldOf(
    "longText",
    "Please leave the parcel with the neighbour at number 12 if nobody answers.",
    label = "Delivery notes",
  )

@CatalogVariant(of = "TextField", props = ["variant=number"], caption = "Numeric keyboard.")
@Preview
@Composable
fun TextFieldNumberSticker() = TextFieldOf("number", "42", label = "Guests")

@CatalogVariant(
  of = "TextField",
  props = ["variant=obscured"],
  caption = "Masked entry, for secrets.",
)
@Preview
@Composable
fun TextFieldObscuredSticker() = TextFieldOf("obscured", "hunter2", label = "Password")

@CatalogVariant(
  of = "TextField",
  state = "empty",
  caption = "No value yet: the label rests inside.",
)
@Preview
@Composable
fun TextFieldEmptySticker() = TextFieldOf("shortText", "")

@Composable
private fun CheckBoxOf(checked: Boolean) =
  A2uiSticker(
    listOf(
      component("root", "CheckBox", "label" to "Email me a receipt", "value" to bind("/checked"))
    ),
    data = mapOf("checked" to checked),
  )

@CatalogComponent(
  id = "CheckBox",
  // M3 kit `Checkbox` / Checked. The catalog draws a Material `Checkbox` beside its label.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/51859:5629",
  caption = "A labelled boolean bound to the data model.",
)
@Preview
@Composable
fun CheckBoxSticker() = CheckBoxOf(checked = true)

@CatalogVariant(of = "CheckBox", state = "unchecked", caption = "The same box, unchecked.")
@Preview
@Composable
fun CheckBoxUncheckedSticker() = CheckBoxOf(checked = false)

private val SEATS =
  listOf("Window" to "window", "Aisle" to "aisle", "Extra legroom" to "legroom").map { (l, v) ->
    mapOf("label" to l, "value" to v)
  }

@Composable
private fun ChoicePickerOf(
  variant: String,
  displayStyle: String,
  selected: List<String>,
  label: String = "Seat preference",
) =
  A2uiSticker(
    listOf(
      component(
        "root",
        "ChoicePicker",
        "label" to label,
        "variant" to variant,
        "displayStyle" to displayStyle,
        "options" to SEATS,
        "value" to bind("/selected"),
      )
    ),
    data = mapOf("selected" to selected),
  )

@CatalogComponent(
  id = "ChoicePicker",
  // M3 kit `Chip` / Filter — `displayStyle: chips` draws each option as a `FilterChip`.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/53923:28465",
  caption = "Pick from a list of options. Chips, allowing several selections.",
)
@Preview
@Composable
fun ChoicePickerSticker() =
  ChoicePickerOf("multipleSelection", "chips", listOf("window", "legroom"))

@CatalogVariant(
  of = "ChoicePicker",
  props = ["variant=mutuallyExclusive"],
  caption = "Chips, one selection at a time.",
)
@Preview
@Composable
fun ChoicePickerExclusiveSticker() = ChoicePickerOf("mutuallyExclusive", "chips", listOf("aisle"))

@CatalogVariant(
  of = "ChoicePicker",
  props = ["displayStyle=checkbox"],
  caption = "An exposed dropdown summarising the selection; the options open in a menu.",
  // `displayStyle: checkbox` draws an `ExposedDropdownMenuBox` whose anchor is an outlined text
  // field — a composition the kit has no single node for.
  noReference = "Exposed dropdown anchored on an outlined text field; no single kit node.",
)
@Preview
@Composable
fun ChoicePickerDropdownSticker() =
  ChoicePickerOf("multipleSelection", "checkbox", listOf("window", "legroom"))

@CatalogComponent(
  id = "Slider",
  // M3 kit `Slider` / Continuous. The catalog draws a Material `Slider` under its label.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/58008:10357",
  caption = "A labelled value between `min` and `max`, bound to the data model.",
)
@Preview
@Composable
fun SliderSticker() =
  A2uiSticker(
    listOf(
      component(
        "root",
        "Slider",
        "label" to "Volume",
        "min" to 0,
        "max" to 100,
        "value" to bind("/volume"),
      )
    ),
    data = mapOf("volume" to 40),
  )

@Composable
private fun DateTimeOf(date: Boolean, time: Boolean, label: String) =
  A2uiSticker(
    listOf(
      component(
        "root",
        "DateTimeInput",
        "label" to label,
        "enableDate" to date,
        "enableTime" to time,
        "value" to bind("/when"),
      )
    ),
    data = mapOf("when" to "2026-09-03T14:30:00Z"),
  )

@CatalogComponent(
  id = "DateTimeInput",
  // M3 kit `Chip` / Assist — at rest the catalog draws the chosen value as an `AssistChip`; the
  // Material date and time picker dialogs open from it.
  reference = "figma:ocdacdEsnHipMJD3egzxKb/53923:28267",
  caption = "A date and time, shown as a chip that opens the Material pickers.",
)
@Preview
@Composable
fun DateTimeInputSticker() = DateTimeOf(date = true, time = true, label = "Departure")

@CatalogVariant(of = "DateTimeInput", props = ["enableTime=false"], caption = "Date only.")
@Preview
@Composable
fun DateTimeInputDateSticker() = DateTimeOf(date = true, time = false, label = "Check-in")

@CatalogVariant(of = "DateTimeInput", props = ["enableDate=false"], caption = "Time only.")
@Preview
@Composable
fun DateTimeInputTimeSticker() = DateTimeOf(date = false, time = true, label = "Alarm")
