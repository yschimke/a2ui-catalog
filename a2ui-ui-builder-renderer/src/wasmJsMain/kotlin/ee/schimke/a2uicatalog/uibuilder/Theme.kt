@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ee.schimke.a2uicatalog.uibuilder

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font

internal val lightScheme = lightColorScheme()
internal val darkScheme = darkColorScheme()

/**
 * Fetches the two Roboto weights Material 3's type scale uses from `fonts/`, beside this page.
 *
 * Skiko on Wasm has no system fonts: without a family every A2UI `Text` measures and draws nothing.
 * The files are compose-ui-builder's vendored `assets/rc-fonts`, the same Roboto the editor draws
 * with, so a label wraps where it does on the rest of the canvas.
 */
internal fun loadRoboto(onLoaded: (FontFamily) -> Unit, onFailed: (String) -> Unit) {
  fetchFonts(
    onLoaded = { regular, medium ->
      onLoaded(
        FontFamily(
          Font("Roboto-Regular", regular.toByteArray(), FontWeight.Normal),
          Font("Roboto-Medium", medium.toByteArray(), FontWeight.Medium),
        )
      )
    },
    onFailed = { onFailed(it.toString()) },
  )
}

/** Material 3's default type scale, every style set in [family]. */
internal fun robotoTypography(family: FontFamily): Typography {
  val base = Typography()
  fun TextStyle.roboto() = copy(fontFamily = family)
  return Typography(
    displayLarge = base.displayLarge.roboto(),
    displayMedium = base.displayMedium.roboto(),
    displaySmall = base.displaySmall.roboto(),
    headlineLarge = base.headlineLarge.roboto(),
    headlineMedium = base.headlineMedium.roboto(),
    headlineSmall = base.headlineSmall.roboto(),
    titleLarge = base.titleLarge.roboto(),
    titleMedium = base.titleMedium.roboto(),
    titleSmall = base.titleSmall.roboto(),
    bodyLarge = base.bodyLarge.roboto(),
    bodyMedium = base.bodyMedium.roboto(),
    bodySmall = base.bodySmall.roboto(),
    labelLarge = base.labelLarge.roboto(),
    labelMedium = base.labelMedium.roboto(),
    labelSmall = base.labelSmall.roboto(),
  )
}

private fun JsAny.toByteArray(): ByteArray =
  ByteArray(byteLength(this)) { byteAt(this, it).toByte() }

@JsFun(
  """(onLoaded, onFailed) => {
    const load = (file) => fetch('fonts/' + file).then((response) => {
      if (!response.ok) throw new Error(file + ': HTTP ' + response.status);
      return response.arrayBuffer();
    }).then((buffer) => new Int8Array(buffer));
    Promise.all([load('Roboto-Regular.ttf'), load('Roboto-Medium.ttf')])
      .then(([regular, medium]) => onLoaded(regular, medium), (error) => onFailed(String(error)));
  }"""
)
private external fun fetchFonts(onLoaded: (JsAny, JsAny) -> Unit, onFailed: (JsString) -> Unit)

@JsFun("(array) => array.length") private external fun byteLength(array: JsAny): Int

@JsFun("(array, index) => array[index]") private external fun byteAt(array: JsAny, index: Int): Int
