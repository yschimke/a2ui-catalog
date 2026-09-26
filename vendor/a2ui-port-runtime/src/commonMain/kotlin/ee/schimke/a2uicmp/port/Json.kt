// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** `java.io.StringReader`: here only a holder for the text a [JsonReader] reads. */
public class StringReader(internal val text: String)

/** `android.util.JsonToken`. */
public enum class JsonToken {
    BEGIN_ARRAY,
    END_ARRAY,
    BEGIN_OBJECT,
    END_OBJECT,
    NAME,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL,
    END_DOCUMENT,
}

/**
 * `android.util.JsonReader`, with the same pull API, over a `kotlinx.serialization` JSON tree.
 *
 * The one behavioural difference from the Android class: the document is parsed in full when the
 * reader is created, so malformed JSON throws from the constructor rather than from the first
 * `peek()`, and the tree is held in memory. Upstream constructs its readers inside the `try` that
 * turns any exception into an `A2uiValidationException`, so the error a caller sees is the same.
 */
public class JsonReader(reader: StringReader) : AutoCloseable {

    private sealed interface Frame

    private class ObjectFrame(obj: JsonObject) : Frame {
        val entries = obj.entries.toList()
        var index = 0
        var nameConsumed = false
    }

    private class ArrayFrame(array: JsonArray) : Frame {
        val items = array
        var index = 0
    }

    private class DocumentFrame(var value: JsonElement?) : Frame

    private val stack = ArrayDeque<Frame>()

    init {
        stack.addLast(DocumentFrame(Json.parseToJsonElement(reader.text)))
    }

    /** The value the cursor is on, or null when it is on a name, a container end or the end. */
    private fun pendingValue(): JsonElement? =
        when (val frame = stack.last()) {
            is DocumentFrame -> frame.value
            is ObjectFrame ->
                if (frame.nameConsumed) frame.entries[frame.index].value else null
            is ArrayFrame -> frame.items.getOrNull(frame.index)
        }

    public fun peek(): JsonToken {
        when (val frame = stack.last()) {
            is DocumentFrame -> if (frame.value == null) return JsonToken.END_DOCUMENT
            is ObjectFrame ->
                if (!frame.nameConsumed) {
                    return if (frame.index < frame.entries.size) JsonToken.NAME
                    else JsonToken.END_OBJECT
                }
            is ArrayFrame -> if (frame.index >= frame.items.size) return JsonToken.END_ARRAY
        }
        return when (val value = pendingValue()!!) {
            is JsonObject -> JsonToken.BEGIN_OBJECT
            is JsonArray -> JsonToken.BEGIN_ARRAY
            JsonNull -> JsonToken.NULL
            is JsonPrimitive ->
                when {
                    value.isString -> JsonToken.STRING
                    value.content == "true" || value.content == "false" -> JsonToken.BOOLEAN
                    else -> JsonToken.NUMBER
                }
        }
    }

    private fun expect(token: JsonToken) {
        val actual = peek()
        check(actual == token) { "Expected $token but was $actual" }
    }

    /** Consumes the pending value: advances whichever frame the cursor is in. */
    private fun advance() {
        when (val frame = stack.last()) {
            is DocumentFrame -> frame.value = null
            is ObjectFrame -> {
                frame.index++
                frame.nameConsumed = false
            }
            is ArrayFrame -> frame.index++
        }
    }

    public fun beginObject() {
        expect(JsonToken.BEGIN_OBJECT)
        val obj = pendingValue() as JsonObject
        stack.addLast(ObjectFrame(obj))
    }

    public fun endObject() {
        expect(JsonToken.END_OBJECT)
        stack.removeLast()
        advance()
    }

    public fun beginArray() {
        expect(JsonToken.BEGIN_ARRAY)
        val array = pendingValue() as JsonArray
        stack.addLast(ArrayFrame(array))
    }

    public fun endArray() {
        expect(JsonToken.END_ARRAY)
        stack.removeLast()
        advance()
    }

    public fun hasNext(): Boolean {
        val token = peek()
        return token != JsonToken.END_OBJECT &&
            token != JsonToken.END_ARRAY &&
            token != JsonToken.END_DOCUMENT
    }

    public fun nextName(): String {
        expect(JsonToken.NAME)
        val frame = stack.last() as ObjectFrame
        frame.nameConsumed = true
        return frame.entries[frame.index].key
    }

    /** The string, or a number's literal text, as `android.util.JsonReader.nextString` does. */
    public fun nextString(): String {
        val token = peek()
        check(token == JsonToken.STRING || token == JsonToken.NUMBER) {
            "Expected a string but was $token"
        }
        val value = (pendingValue() as JsonPrimitive).content
        advance()
        return value
    }

    public fun nextBoolean(): Boolean {
        expect(JsonToken.BOOLEAN)
        val value = (pendingValue() as JsonPrimitive).content == "true"
        advance()
        return value
    }

    public fun nextNull() {
        expect(JsonToken.NULL)
        advance()
    }

    private fun nextNumberText(): String {
        val token = peek()
        if (token != JsonToken.NUMBER && token != JsonToken.STRING) {
            throw IllegalStateException("Expected a number but was $token")
        }
        return (pendingValue() as JsonPrimitive).content
    }

    public fun nextDouble(): Double {
        val value =
            nextNumberText().toDoubleOrNull() ?: throw NumberFormatException(nextNumberText())
        advance()
        return value
    }

    public fun nextLong(): Long {
        val text = nextNumberText()
        val value =
            text.toLongOrNull()
                ?: text.toDoubleOrNull()?.takeIf { it == it.toLong().toDouble() }?.toLong()
                ?: throw NumberFormatException(text)
        advance()
        return value
    }

    public fun nextInt(): Int {
        val text = nextNumberText()
        val value =
            text.toIntOrNull()
                ?: text.toDoubleOrNull()?.takeIf { it == it.toInt().toDouble() }?.toInt()
                ?: throw NumberFormatException(text)
        advance()
        return value
    }

    public fun skipValue() {
        if (peek() == JsonToken.NAME) {
            (stack.last() as ObjectFrame).nameConsumed = true
        }
        advance()
    }

    override fun close() {
        stack.clear()
        stack.addLast(DocumentFrame(null))
    }
}
