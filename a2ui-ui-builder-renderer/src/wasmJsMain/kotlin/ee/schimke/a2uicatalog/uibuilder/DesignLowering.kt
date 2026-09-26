package ee.schimke.a2uicatalog.uibuilder

import ee.schimke.composeai.uibuilder.export.A2uiDocumentExporter
import ee.schimke.composeai.uibuilder.export.UiBuilderDocument
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** A design as the renderer draws it: A2UI messages, or why it cannot be sent as A2UI. */
internal sealed interface LoweredDesign {
  /**
   * @param messages the JSON messages, in send order, for one surface.
   * @param designNodeIds A2UI component id → the design node it came from. They differ only for the
   *   root, which A2UI always calls `root`.
   */
  data class Messages(
    val surfaceId: String,
    val messages: List<String>,
    val designNodeIds: Map<String, String>,
  ) : LoweredDesign

  data class Refused(val reasons: List<String>) : LoweredDesign
}

/** The one surface every render draws; a render never shares a processor with another. */
internal const val SURFACE_ID = "ui-builder"

/**
 * Lowers [document] exactly as the ui-builder's JSON export does (`A2uiDocumentExporter.lower`), so
 * the canvas and the export cannot disagree about a design, then marks every component with the
 * design node it came from ([NODE_ID_PROPERTY]). The marker is how a drawn component reports its
 * bounds: A2UI hands a component its properties, never its id.
 */
internal fun lowerDesign(document: UiBuilderDocument): LoweredDesign {
  val lowered =
    when (val result = A2uiDocumentExporter.lower(document)) {
      is A2uiDocumentExporter.Lowered.Refused -> return LoweredDesign.Refused(result.reasons)
      is A2uiDocumentExporter.Lowered.Components -> result
    }
  val rootId = document.roots.single()
  val designNodeIds = mutableMapOf<String, String>()
  val components =
    lowered.components.map { component ->
      val id = (component["id"] as JsonPrimitive).content
      val designId = if (id == ROOT_ID) rootId else id
      designNodeIds[id] = designId
      JsonObject(component + (NODE_ID_PROPERTY to JsonPrimitive(id)))
    }
  val messages = buildList {
    add(
      envelope(
        "createSurface",
        buildJsonObject {
          put("surfaceId", SURFACE_ID)
          put("catalogId", A2uiDocumentExporter.BASIC_CATALOG_ID)
        },
      )
    )
    if (lowered.data.isNotEmpty()) {
      add(
        envelope(
          "updateDataModel",
          buildJsonObject {
            put("surfaceId", SURFACE_ID)
            put("path", "/")
            put("value", lowered.data)
          },
        )
      )
    }
    add(
      envelope(
        "updateComponents",
        buildJsonObject {
          put("surfaceId", SURFACE_ID)
          put("components", JsonArray(components))
        },
      )
    )
  }
  return LoweredDesign.Messages(SURFACE_ID, messages.map(JsonObject::toString), designNodeIds)
}

private const val ROOT_ID = "root"

private fun envelope(kind: String, body: JsonObject): JsonObject = buildJsonObject {
  put("version", "v0.9")
  put(kind, body)
}
