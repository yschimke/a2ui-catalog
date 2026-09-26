package ee.schimke.a2uicatalog.samples

import androidx.compose.material3.integration.a2ui.DemoTheme
import androidx.compose.material3.integration.a2ui.model.UiComponent
import androidx.compose.material3.integration.a2ui.ui.ComponentListScreen
import androidx.compose.material3.integration.a2ui.ui.samples.AudioPlayerSample
import androidx.compose.material3.integration.a2ui.ui.samples.ButtonSample
import androidx.compose.material3.integration.a2ui.ui.samples.CardSample
import androidx.compose.material3.integration.a2ui.ui.samples.CheckBoxSample
import androidx.compose.material3.integration.a2ui.ui.samples.ChoicePickerSample
import androidx.compose.material3.integration.a2ui.ui.samples.ColumnSample
import androidx.compose.material3.integration.a2ui.ui.samples.DateTimeInputSample
import androidx.compose.material3.integration.a2ui.ui.samples.DividerSample
import androidx.compose.material3.integration.a2ui.ui.samples.IconSample
import androidx.compose.material3.integration.a2ui.ui.samples.ImageSample
import androidx.compose.material3.integration.a2ui.ui.samples.ListSample
import androidx.compose.material3.integration.a2ui.ui.samples.ModalSample
import androidx.compose.material3.integration.a2ui.ui.samples.RowSample
import androidx.compose.material3.integration.a2ui.ui.samples.SliderSample
import androidx.compose.material3.integration.a2ui.ui.samples.TabsSample
import androidx.compose.material3.integration.a2ui.ui.samples.TextFieldSample
import androidx.compose.material3.integration.a2ui.ui.samples.TextSample
import androidx.compose.material3.integration.a2ui.ui.samples.VideoSample
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ee.schimke.composeai.preview.CatalogComponent

// One `@Preview` per vendored `<Component>Sample`, in the demo's own order (`UiComponent`) and
// under the demo's own three categories. The sample bodies are upstream's; only the screen around
// them (SampleScreen.kt) and these annotations are this repo's — which is why the inventory can be
// annotated here rather than generated: nothing in this file is re-fetched by an import.
//
// Each card names its a2ui-catalog counterpart through `related`, so the served sheet links a
// component's sticker to the sample screen that exercises it, and back.
//
// A phone-sized frame, because the sample is a SCREEN: the preview card is 240dp tall and the
// controls stack beneath it, as on the compact-width layout of the demo.

private const val SAMPLE = "An AndroidX sample screen, not a design reproduction."

@CatalogComponent(
  id = "ComponentList",
  group = "Index",
  noReference = SAMPLE,
  caption = "The demo's index: every basic-catalog component, by category.",
)
@Preview(name = "Component list", widthDp = 412, heightDp = 915)
@Composable
fun ComponentListPreview() = DemoTheme { ComponentListScreen(onComponentSelected = {}) }

@CatalogComponent(
  id = "Row",
  group = "Layout",
  noReference = SAMPLE,
  caption = "`RowSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Row", widthDp = 412, heightDp = 915)
@Composable
fun RowSamplePreview() = SampleScreen(UiComponent.ROW) { RowSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Column",
  group = "Layout",
  noReference = SAMPLE,
  caption = "`ColumnSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Column", widthDp = 412, heightDp = 915)
@Composable
fun ColumnSamplePreview() = SampleScreen(UiComponent.COLUMN) { ColumnSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "List",
  group = "Layout",
  noReference = SAMPLE,
  caption = "`ListSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "List", widthDp = 412, heightDp = 915)
@Composable
fun ListSamplePreview() = SampleScreen(UiComponent.LIST) { ListSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Card",
  group = "Layout",
  noReference = SAMPLE,
  caption = "`CardSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Card", widthDp = 412, heightDp = 915)
@Composable
fun CardSamplePreview() = SampleScreen(UiComponent.CARD) { CardSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Tabs",
  group = "Layout",
  noReference = SAMPLE,
  caption = "`TabsSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Tabs", widthDp = 412, heightDp = 915)
@Composable
fun TabsSamplePreview() = SampleScreen(UiComponent.TABS) { TabsSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Modal",
  group = "Layout",
  noReference = SAMPLE,
  caption = "`ModalSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Modal", widthDp = 412, heightDp = 915)
@Composable
fun ModalSamplePreview() = SampleScreen(UiComponent.MODAL) { ModalSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Text",
  group = "Content",
  noReference = SAMPLE,
  caption = "`TextSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Text", widthDp = 412, heightDp = 915)
@Composable
fun TextSamplePreview() = SampleScreen(UiComponent.TEXT) { TextSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Image",
  group = "Content",
  noReference = SAMPLE,
  caption = "`ImageSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Image", widthDp = 412, heightDp = 915)
@Composable
fun ImageSamplePreview() = SampleScreen(UiComponent.IMAGE) { ImageSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Icon",
  group = "Content",
  noReference = SAMPLE,
  caption = "`IconSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Icon", widthDp = 412, heightDp = 915)
@Composable
fun IconSamplePreview() = SampleScreen(UiComponent.ICON) { IconSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Divider",
  group = "Content",
  noReference = SAMPLE,
  caption = "`DividerSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Divider", widthDp = 412, heightDp = 915)
@Composable
fun DividerSamplePreview() =
  SampleScreen(UiComponent.DIVIDER) { DividerSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Video",
  group = "Content",
  noReference = SAMPLE,
  caption = "`VideoSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Video", widthDp = 412, heightDp = 915)
@Composable
fun VideoSamplePreview() = SampleScreen(UiComponent.VIDEO) { VideoSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "AudioPlayer",
  group = "Content",
  noReference = SAMPLE,
  caption = "`AudioPlayerSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "AudioPlayer", widthDp = 412, heightDp = 915)
@Composable
fun AudioPlayerSamplePreview() =
  SampleScreen(UiComponent.AUDIO_PLAYER) { AudioPlayerSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Button",
  group = "Input",
  noReference = SAMPLE,
  caption = "`ButtonSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Button", widthDp = 412, heightDp = 915)
@Composable
fun ButtonSamplePreview() = SampleScreen(UiComponent.BUTTON) { ButtonSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "CheckBox",
  group = "Input",
  noReference = SAMPLE,
  caption = "`CheckBoxSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "CheckBox", widthDp = 412, heightDp = 915)
@Composable
fun CheckBoxSamplePreview() =
  SampleScreen(UiComponent.CHECK_BOX) { CheckBoxSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "Slider",
  group = "Input",
  noReference = SAMPLE,
  caption = "`SliderSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "Slider", widthDp = 412, heightDp = 915)
@Composable
fun SliderSamplePreview() = SampleScreen(UiComponent.SLIDER) { SliderSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "TextField",
  group = "Input",
  noReference = SAMPLE,
  caption = "`TextFieldSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "TextField", widthDp = 412, heightDp = 915)
@Composable
fun TextFieldSamplePreview() =
  SampleScreen(UiComponent.TEXT_FIELD) { TextFieldSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "DateTimeInput",
  group = "Input",
  noReference = SAMPLE,
  caption = "`DateTimeInputSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "DateTimeInput", widthDp = 412, heightDp = 915)
@Composable
fun DateTimeInputSamplePreview() =
  SampleScreen(UiComponent.DATE_TIME_INPUT) { DateTimeInputSample(onPayloadUpdated = it) }

@CatalogComponent(
  id = "ChoicePicker",
  group = "Input",
  noReference = SAMPLE,
  caption = "`ChoicePickerSample` at its initial settings, above the payload it produces.",
  related = ["a2ui-catalog"],
)
@Preview(name = "ChoicePicker", widthDp = 412, heightDp = 915)
@Composable
fun ChoicePickerSamplePreview() =
  SampleScreen(UiComponent.CHOICE_PICKER) { ChoicePickerSample(onPayloadUpdated = it) }
