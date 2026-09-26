package ee.schimke.a2uicatalog.harness

import androidx.a2ui.compose.runtime.A2uiMessageParser
import androidx.a2ui.model.processor.A2uiMessageParser
import androidx.a2ui.model.protocol.A2uiCreateSurfaceMessage
import androidx.a2ui.model.protocol.A2uiException
import androidx.a2ui.model.protocol.A2uiServerToClientMessage
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/**
 * An A2UI document as a person or a tool would paste it, parsed by AndroidX's own message parser.
 *
 * Three spellings are accepted, because each is what some producer naturally has in hand:
 * * **JSON Lines** — one server-to-client message per line, the framing A2UI streams in and what
 *   compose-ui-builder's `A2uiDocumentExporter` writes;
 * * **a JSON array** of the same messages;
 * * **a components shorthand** — `{"components": [...]}` (optionally with `"data"`), expanded to
 *   `createSurface` + `updateDataModel` + `updateComponents` on the basic catalog, so a component
 *   list can be tried without writing the envelopes.
 *
 * Parsing never throws: every message the parser rejects is kept as an [error], and the messages it
 * accepted still render, so a document with one typo shows everything else plus the typo.
 */
class A2uiDocument
private constructor(
  val messages: List<A2uiServerToClientMessage>,
  val errors: List<String>,
) {
  /** The first surface the document creates, which is the one a preview draws. */
  val surfaceId: String?
    get() = messages.firstNotNullOfOrNull { (it as? A2uiCreateSurfaceMessage)?.surfaceId }

  companion object {
    fun parse(
      source: String,
      catalogId: String = StickerCatalog.id,
      parser: A2uiMessageParser<String> = A2uiMessageParser(),
    ): A2uiDocument {
      val raw =
        try {
          split(source.trim(), catalogId)
        } catch (e: Exception) {
          return A2uiDocument(emptyList(), listOf("Not a JSON document: ${e.message}"))
        }
      val messages = mutableListOf<A2uiServerToClientMessage>()
      val errors = mutableListOf<String>()
      raw.forEachIndexed { index, json ->
        try {
          messages += parser.parse(json)
        } catch (e: A2uiException) {
          errors += "message ${index + 1}: ${e.message} (${e.code} at ${e.context["path"] ?: "/"})"
        }
      }
      if (messages.none { it is A2uiCreateSurfaceMessage } && errors.isEmpty()) {
        errors += "The document creates no surface: send a `createSurface` message first."
      }
      return A2uiDocument(messages, errors)
    }

    /** The document as one JSON string per message. */
    private fun split(source: String, catalogId: String): List<String> {
      if (source.isEmpty()) return emptyList()
      if (source.startsWith("[")) {
        val array = JSONArray(source)
        return List(array.length()) { array.get(it).toString() }
      }
      // Probe only for the shorthand. A first value that does not parse is not a document-level
      // failure: in JSON Lines it is one bad message, reported per line below while the rest
      // render.
      val first = runCatching { JSONTokener(source).nextValue() }.getOrNull()
      if (first is JSONObject && !first.has("version") && first.has("components")) {
        return shorthand(first, catalogId)
      }
      return source.lines().map(String::trim).filter(String::isNotEmpty)
    }

    private fun shorthand(document: JSONObject, catalogId: String): List<String> {
      val surfaceId = document.optString("surfaceId", StickerHost.DEFAULT_SURFACE_ID)
      fun envelope(kind: String, body: JSONObject) =
        JSONObject().put("version", "v0.9").put(kind, body.put("surfaceId", surfaceId)).toString()
      return buildList {
        add(envelope("createSurface", JSONObject().put("catalogId", catalogId)))
        document.optJSONObject("data")?.let {
          add(envelope("updateDataModel", JSONObject().put("path", "/").put("value", it)))
        }
        add(
          envelope(
            "updateComponents",
            JSONObject().put("components", document.getJSONArray("components")),
          )
        )
      }
    }
  }
}
