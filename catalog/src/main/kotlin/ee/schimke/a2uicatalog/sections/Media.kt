@file:CatalogGroup(name = "Media", section = "Content")

package ee.schimke.a2uicatalog.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ee.schimke.a2uicatalog.harness.A2uiSticker
import ee.schimke.a2uicatalog.harness.component
import ee.schimke.composeai.preview.CatalogComponent
import ee.schimke.composeai.preview.CatalogGroup
import ee.schimke.composeai.preview.CatalogVariant

// The three media components take their renderer from the APP — `material3-a2ui` decides the frame
// (an Image variant's size and clip, for instance) and the app decides the pixels inside it. Here
// the pixels are `StickerMedia`'s deterministic artwork, so what these stickers document is the
// catalog's frame, and none of them has a kit node: the kit draws no media component.

private const val PHOTO = "https://example.com/a2ui/lisbon.jpg"

@Composable
private fun ImageOf(variant: String, fit: String = "cover") =
  A2uiSticker(
    listOf(
      component(
        "root",
        "Image",
        "url" to PHOTO,
        "description" to "Tram on a Lisbon street",
        "variant" to variant,
        "fit" to fit,
      )
    )
  )

@CatalogComponent(
  id = "Image",
  noReference = "The picture is the app's renderer; the kit publishes no image component.",
  caption =
    "An image at one of six catalog sizes. `mediumFeature` by default: 128dp, 12dp corners.",
)
@Preview
@Composable
fun ImageSticker() = ImageOf("mediumFeature")

@CatalogVariant(of = "Image", props = ["variant=icon"], caption = "24dp, unclipped.")
@Preview
@Composable
fun ImageIconSticker() = ImageOf("icon")

@CatalogVariant(of = "Image", props = ["variant=avatar"], caption = "40dp, clipped to a circle.")
@Preview
@Composable
fun ImageAvatarSticker() = ImageOf("avatar")

@CatalogVariant(of = "Image", props = ["variant=smallFeature"], caption = "64dp, 8dp corners.")
@Preview
@Composable
fun ImageSmallFeatureSticker() = ImageOf("smallFeature")

@CatalogVariant(of = "Image", props = ["variant=largeFeature"], caption = "256dp, 16dp corners.")
@Preview
@Composable
fun ImageLargeFeatureSticker() = ImageOf("largeFeature")

@CatalogVariant(of = "Image", props = ["variant=header"], caption = "Full width, 200dp tall.")
@Preview
@Composable
fun ImageHeaderSticker() = ImageOf("header")

@CatalogComponent(
  id = "Video",
  noReference = "The player is the app's renderer; the kit publishes no video component.",
  caption = "A video, shown at rest as its poster frame.",
)
@Preview
@Composable
fun VideoSticker() =
  A2uiSticker(listOf(component("root", "Video", "url" to "https://example.com/a2ui/tour.mp4")))

@CatalogComponent(
  id = "AudioPlayer",
  noReference = "The player is the app's renderer; the kit publishes no audio component.",
  caption = "An audio track with its description, at rest.",
)
@Preview
@Composable
fun AudioPlayerSticker() =
  A2uiSticker(
    listOf(
      component(
        "root",
        "AudioPlayer",
        "url" to "https://example.com/a2ui/briefing.mp3",
        "description" to "Morning briefing",
      )
    )
  )
