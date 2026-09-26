package ee.schimke.a2uicatalog

import ee.schimke.a2uicatalog.harness.BasicCatalogComponents
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Holds the sheet to the library: one `@CatalogComponent` per component of the A2UI basic catalog,
 * each either mapped to an M3 kit node or saying why it is not.
 *
 * Read off the source rather than off `previews.json` so it runs in `test` without a render, the
 * same way m3-catalog's inventory test reads its annotations.
 */
class CatalogInventoryTest {

  private val sections = File("src/main/kotlin/ee/schimke/a2uicatalog/sections")

  private val components: List<Map<String, String>> by lazy {
    val block = Regex("""@CatalogComponent\((.*?)\)\s*@Preview""", RegexOption.DOT_MATCHES_ALL)
    val field = Regex("""(\w+)\s*=\s*"([^"]*)"""")
    sections
      .listFiles { f -> f.extension == "kt" }!!
      .sortedBy { it.name }
      .flatMap { file -> block.findAll(file.readText()).map { it.groupValues[1] } }
      .map { body -> field.findAll(body).associate { it.groupValues[1] to it.groupValues[2] } }
  }

  @Test
  fun `every basic catalog component has exactly one card`() {
    val ids = components.map { it.getValue("id") }
    assertEquals(ids.distinct(), ids, "a component id is declared twice")
    assertEquals(BasicCatalogComponents.sorted(), ids.sorted())
  }

  @Test
  fun `every component is either mapped to the kit or says why not`() {
    for (component in components) {
      val id = component.getValue("id")
      val reference = component["reference"].orEmpty()
      val noReference = component["noReference"].orEmpty()
      assertTrue(
        (reference.isEmpty()) != (noReference.isEmpty()),
        "$id must carry exactly one of `reference` or `noReference`",
      )
      if (reference.isNotEmpty()) {
        assertTrue(
          reference.matches(Regex("""figma:ocdacdEsnHipMJD3egzxKb/\d+:\d+""")),
          "$id: `$reference` is not a node of the M3 Design Kit",
        )
      }
    }
  }
}
