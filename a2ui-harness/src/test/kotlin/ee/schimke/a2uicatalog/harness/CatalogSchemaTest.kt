package ee.schimke.a2uicatalog.harness

import androidx.a2ui.compose.ui.toJsonSchemaString
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `a2ui-basic-catalog.schema.json` at the repository root is the catalog's own JSON Schema, exactly
 * as `material3-a2ui` advertises it to an agent during capability negotiation
 * (`A2uiCatalog.toJsonSchemaString()`). It is what the ui-builder policy and the builder's A2UI
 * exporter are written against, so it is committed and held to the library here: a library bump
 * that changes a property, an enum or a required flag fails this test with the new schema written
 * in place, and the diff is the review.
 *
 * Regenerate with `./gradlew :a2ui-harness:testDebugUnitTest -Pa2ui.updateSchema=true`, or just run
 * the test and commit the file it rewrote.
 */
class CatalogSchemaTest {

  @Test
  fun `the committed catalog schema is the one the library advertises`() {
    val committed = File("../a2ui-basic-catalog.schema.json")
    val actual = pretty(StickerCatalog.toJsonSchemaString()) + "\n"
    val before = if (committed.isFile) committed.readText() else ""
    if (before != actual) committed.writeText(actual)
    assertEquals(
      before,
      actual,
      "a2ui-basic-catalog.schema.json was stale and has been rewritten; commit the new file.",
    )
  }

  /** Two-space JSON, keys in the library's order, so the committed file diffs line by line. */
  private fun pretty(json: String): String {
    val out = StringBuilder()
    var indent = 0
    var inString = false
    var escaped = false
    fun newline() {
      out.append('\n')
      repeat(indent) { out.append("  ") }
    }
    for (c in json) {
      if (inString) {
        out.append(c)
        when {
          escaped -> escaped = false
          c == '\\' -> escaped = true
          c == '"' -> inString = false
        }
        continue
      }
      when (c) {
        '"' -> {
          inString = true
          out.append(c)
        }
        '{',
        '[' -> {
          out.append(c)
          indent++
          newline()
        }
        '}',
        ']' -> {
          indent--
          newline()
          out.append(c)
        }
        ',' -> {
          out.append(c)
          newline()
        }
        ':' -> out.append(": ")
        ' ',
        '\n',
        '\t',
        '\r' -> {}
        else -> out.append(c)
      }
    }
    return out.toString().replace(Regex("""\{\s+\}"""), "{}").replace(Regex("""\[\s+\]"""), "[]")
  }
}
