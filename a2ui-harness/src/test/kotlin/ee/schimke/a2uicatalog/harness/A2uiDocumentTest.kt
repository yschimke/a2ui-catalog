package ee.schimke.a2uicatalog.harness

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Robolectric because the parser reads JSON through Android's `JsonReader` and `org.json`. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class A2uiDocumentTest {

  private val card =
    """[{"id":"root","component":"Card","child":"t"},{"id":"t","component":"Text","text":"Hi"}]"""

  private fun rendered(document: A2uiDocument) {
    val host = StickerHost(StickerCatalog)
    try {
      assertNotNull(host.show(document), "errors: ${document.errors}")
    } finally {
      host.close()
    }
  }

  @Test
  fun `JSON Lines messages parse and render`() {
    val document =
      A2uiDocument.parse(
        """
        {"version":"v0.9","createSurface":{"surfaceId":"s","catalogId":"${StickerCatalog.id}"}}
        {"version":"v0.9","updateComponents":{"surfaceId":"s","components":$card}}
        """
      )
    assertEquals(emptyList(), document.errors)
    assertEquals("s", document.surfaceId)
    rendered(document)
  }

  @Test
  fun `a JSON array of messages parses`() {
    val document =
      A2uiDocument.parse(
        """[{"version":"v0.9","createSurface":{"surfaceId":"s","catalogId":"${StickerCatalog.id}"}},
            {"version":"v0.9","updateComponents":{"surfaceId":"s","components":$card}}]"""
      )
    assertEquals(2, document.messages.size)
    rendered(document)
  }

  @Test
  fun `the components shorthand expands to a surface on the basic catalog`() {
    val document = A2uiDocument.parse("""{"components":$card,"data":{"name":"Ada"}}""")
    assertEquals(emptyList(), document.errors)
    assertEquals(3, document.messages.size)
    rendered(document)
  }

  @Test
  fun `a rejected message is reported and the rest still parse`() {
    val document =
      A2uiDocument.parse(
        """
        {"version":"v0.9","createSurface":{"surfaceId":"s","catalogId":"${StickerCatalog.id}"}}
        {"version":"v7","updateComponents":{"surfaceId":"s","components":[]}}
        """
      )
    assertEquals(1, document.messages.size)
    assertTrue(document.errors.single().startsWith("message 2:"), document.errors.toString())
  }

  @Test
  fun `malformed JSON is an error, not an exception`() {
    val document = A2uiDocument.parse("[{")
    assertTrue(
      document.errors.single().startsWith("Not a JSON document"),
      document.errors.toString(),
    )
  }
}
