package ee.schimke.a2uicatalog.desktop

import androidx.a2ui.compose.ui.catalog.A2uiBasicCatalogV1
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.a2ui.A2uiSurface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The CMP port, end to end, on the desktop JVM: JSON parsed by the port's reader, applied by the
 * vendored engine, drawn by the vendored Material 3 catalog under Skiko. No Robolectric, no Android
 * class on the classpath.
 */
@OptIn(ExperimentalTestApi::class)
class DesktopRenderTest {

  private val createSurface =
    """{"version":"v0.9","createSurface":{"surfaceId":"proof","catalogId":"${A2uiBasicCatalogV1.CatalogId}"}}"""

  private val updateDataModel =
    """{"version":"v0.9","updateDataModel":{"surfaceId":"proof","path":"/","value":{"name":"Ada Lovelace"}}}"""

  private val updateComponents =
    """
    {"version":"v0.9","updateComponents":{"surfaceId":"proof","components":[
      {"id":"root","component":"Card","child":"column"},
      {"id":"column","component":"Column","children":["title","body","name","submit"]},
      {"id":"title","component":"Text","text":"Rendered on Compose Desktop","variant":"h3"},
      {"id":"body","component":"Text","text":"A2UI 1.0.0-alpha01, Compose Multiplatform port, no Robolectric."},
      {"id":"name","component":"TextField","label":"Name","value":{"path":"/name"}},
      {"id":"submit","component":"Button","child":"submit_text","variant":"primary",
        "action":{"event":{"name":"submit"}}},
      {"id":"submit_text","component":"Text","text":"Submit"}
    ]}}
    """
      .trimIndent()

  @Test
  fun `a createSurface and updateComponents payload renders through the vendored Material catalog`() =
    runComposeUiTest {
      val host = DesktopA2uiHost()
      try {
        host.processJson(createSurface, updateDataModel, updateComponents)
        val surface = assertNotNull(host.surface("proof"), "the surface exists once processed")

        setContent {
          MaterialTheme {
            Surface {
              Box(Modifier.testTag("surface").padding(16.dp).width(360.dp)) {
                A2uiSurface(surfaceModel = surface, transitionSpec = null)
              }
            }
          }
        }

        // Semantics: the payload's text, its bound value and the button label all reached the tree.
        onNodeWithText("Rendered on Compose Desktop").assertExists()
        onNodeWithText("Submit").assertExists()
        onNodeWithText("Ada Lovelace", substring = true).assertExists()

        // Pixels: a real, non-blank picture.
        val image = onNodeWithTag("surface").captureToImage().toAwtImage()
        assertTrue(image.width > 100 && image.height > 100, "${image.width}x${image.height}")
        val colours = HashSet<Int>()
        for (y in 0 until image.height step 2) {
          for (x in 0 until image.width step 2) colours += image.getRGB(x, y)
        }
        assertTrue(colours.size > 16, "expected a drawn surface, saw ${colours.size} colours")

        val out = File(System.getProperty("a2ui.desktop.out") ?: "build/a2ui-desktop")
        out.mkdirs()
        val png = File(out, "cmp-desktop-render.png")
        assertTrue(ImageIO.write(image, "png", png))
        assertEquals(true, png.length() > 0)
      } finally {
        host.close()
      }
    }
}
