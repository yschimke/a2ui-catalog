package ee.schimke.a2uicatalog.samples

import androidx.a2ui.model.protocol.A2uiComponentPayload
import ee.schimke.a2uicatalog.harness.A2uiDocument
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A sample's published `document` knob is a message stream the playground's own parser accepts. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PayloadDocumentTest {
  @Test
  fun `a sample payload round-trips through the playground parser`() {
    val source =
      payloadDocument(
        listOf(
          A2uiComponentPayload(
            id = "root",
            type = "Button",
            properties =
              mapOf("child" to "label", "action" to mapOf("event" to mapOf("name" to "go"))),
          ),
          A2uiComponentPayload(id = "label", type = "Text", properties = mapOf("text" to "Go")),
        ),
        mapOf("name" to "Ada", "tags" to listOf("a", "b")),
      )
    assertEquals(3, source.lines().size, source)
    kotlin.test.assertTrue("https://a2ui.org/" in source, source)
    val document = A2uiDocument.parse(source)
    assertEquals(emptyList(), document.errors)
    assertEquals("sample", document.surfaceId)
  }
}
