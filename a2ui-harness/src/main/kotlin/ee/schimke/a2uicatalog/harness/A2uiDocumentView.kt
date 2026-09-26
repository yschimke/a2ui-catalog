package ee.schimke.a2uicatalog.harness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Renders an A2UI document (see [A2uiDocument] for the accepted spellings) with the Material 3
 * basic catalog: the surface it creates, and beneath it every message the parser rejected. This is
 * what the playground preview draws, so a remote render of a pasted document answers both "what
 * does it look like" and "what is wrong with it" in one picture.
 */
@Composable
fun A2uiDocumentView(source: String, modifier: Modifier = Modifier) {
  StickerTheme {
    val host = rememberStickerHost()
    val document = remember(source) { A2uiDocument.parse(source) }
    val surface = remember(host, document) { host.show(document) }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
      if (surface != null) A2uiSurface(surfaceModel = surface, transitionSpec = null)
      if (document.errors.isNotEmpty()) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = MaterialTheme.shapes.medium,
        ) {
          Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            document.errors.forEach {
              Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
              )
            }
          }
        }
      }
    }
  }
}
