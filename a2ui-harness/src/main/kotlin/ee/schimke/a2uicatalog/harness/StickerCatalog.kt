package ee.schimke.a2uicatalog.harness

import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.model.catalog.functions.A2uiLocaleProvider
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
 * `urlOpener` is a no-op because nothing here may navigate, and `messageFormatter` echoes the
 * pattern, which is what the AndroidX samples themselves pass.
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
    urlOpener = {},
    messageFormatter = { pattern, _, _ -> pattern },
    localeProvider = A2uiLocaleProvider.Default,
  )
