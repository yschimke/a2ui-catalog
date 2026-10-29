package ee.schimke.a2uicatalog.harness

import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.a2ui.model.protocol.A2uiExecutionContext
import androidx.compose.material3.a2ui.catalog.MaterialA2uiBasicCatalogV1Defaults
import androidx.compose.material3.a2ui.catalog.materialA2uiBasicCatalogV1

/**
 * The Material 3 A2UI basic catalog exactly as `material3-a2ui` ships it — every one of its 18
 * components at its library default — except for the three the library cannot supply itself.
 *
 * `Image`, `Video` and `AudioPlayer` take a renderer from the app, because loading media is the
 * app's business. The ones here are [StickerMedia]'s, which draw from constants so a render cannot
 * differ between runs or depend on the network. Everything the MATERIAL catalog decides — the size
 * an image `variant` gets, its clip, the audio player's controls — is still the library's.
 *
 * The catalog's functions are [StickerFunctions]: the library defaults, with `openUrl` a no-op.
 */
val StickerCatalog: A2uiCatalog =
  materialA2uiBasicCatalogV1(
    image =
      MaterialA2uiBasicCatalogV1Defaults.image { url, contentDescription, contentScale, modifier, _
        ->
        StickerMedia.Image(url, contentDescription, contentScale, modifier)
      },
    video =
      MaterialA2uiBasicCatalogV1Defaults.video { url, modifier, _ ->
        StickerMedia.Video(url, modifier)
      },
    audioPlayer =
      MaterialA2uiBasicCatalogV1Defaults.audioPlayer { url, contentDescription, modifier, _ ->
        StickerMedia.AudioPlayer(url, contentDescription, modifier)
      },
    functions = StickerFunctions,
  )

/**
 * The basic catalog's functions as a render harness needs them. Since 1.0.0-alpha02 the catalog
 * takes one [A2uiBasicCatalogV1.Functions] in place of the separate `urlOpener`, `messageFormatter`
 * and `localeProvider`. `openUrl` is the only member without a default, and it is a no-op because
 * nothing here may navigate. The formatting functions keep their defaults, which format in the
 * default locale as `A2uiLocaleProvider.Default` did. The context-free form is used deliberately:
 * `A2uiBasicCatalogV1.Functions(context)` would open URLs with a browsable intent.
 */
private object StickerFunctions : A2uiBasicCatalogV1.Functions {
  override fun openUrl(url: String, executionContext: A2uiExecutionContext) {}
}
