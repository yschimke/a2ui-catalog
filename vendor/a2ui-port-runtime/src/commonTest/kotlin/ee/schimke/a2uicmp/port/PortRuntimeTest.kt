// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The seam's contract on every target: what upstream's call sites rely on it to do. */
class PortRuntimeTest {

    @Test
    fun jsonReaderWalksAnEnvelopeLikeAndroidUtilJsonReader() {
        val reader =
            JsonReader(StringReader("""{"version":"v0.9","n":1.5,"i":7,"b":true,"z":null,"a":[1,"x"]}"""))
        reader.beginObject()
        assertEquals("version", reader.nextName())
        assertEquals(JsonToken.STRING, reader.peek())
        assertEquals("v0.9", reader.nextString())
        assertEquals("n", reader.nextName())
        assertEquals(JsonToken.NUMBER, reader.peek())
        assertEquals("1.5", reader.nextString())
        assertEquals("i", reader.nextName())
        assertEquals(7, reader.nextInt())
        assertEquals("b", reader.nextName())
        assertEquals(JsonToken.BOOLEAN, reader.peek())
        assertTrue(reader.nextBoolean())
        assertEquals("z", reader.nextName())
        assertEquals(JsonToken.NULL, reader.peek())
        reader.nextNull()
        assertEquals("a", reader.nextName())
        reader.beginArray()
        assertEquals(1L, reader.nextLong())
        assertTrue(reader.hasNext())
        reader.skipValue()
        assertFalse(reader.hasNext())
        reader.endArray()
        assertFalse(reader.hasNext())
        reader.endObject()
        assertEquals(JsonToken.END_DOCUMENT, reader.peek())
    }

    @Test
    fun malformedJsonThrows() {
        assertFailsWith<Exception> { JsonReader(StringReader("{\"version\":")).peek() }
    }

    @Test
    fun isoPatternsRoundTripInUtc() {
        val utc = TimeZone.getTimeZone("UTC")
        val format =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locales.US).apply { timeZone = utc }
        assertEquals("2026-09-23T13:45:07.089Z", format.format(Date(1_790_171_107_089L)))

        val parser =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locales.US).apply {
                timeZone = utc
                isLenient = false
            }
        assertEquals(1_790_171_107_000L, parser.parse("2026-09-23T13:45:07")!!.time)
        assertFailsWith<ParseException> { parser.parse("23/09/2026") }
    }

    @Test
    fun calendarReadsUtcFields() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = 1_790_171_107_089L
        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(13, calendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        assertEquals(2026, calendar.get(Calendar.YEAR))
        assertEquals(0, calendar.get(Calendar.MINUTE))
    }

    @Test
    fun numberFormatHonoursGroupingAndDigits() {
        val format =
            NumberFormat.getNumberInstance(Locales.US).apply {
                isGroupingUsed = true
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
        assertEquals("1,234,567.89", format.format(1_234_567.891))
        assertEquals("USD", Currency.getInstance("USD").currencyCode)
        assertFailsWith<IllegalArgumentException> { Currency.getInstance("not-a-code") }
    }

    @Test
    fun concurrentMapComputes() {
        val map = ConcurrentHashMap<String, Int>()
        assertEquals(1, map.computeIfAbsent("a") { 1 })
        assertEquals(1, map.computeIfAbsent("a") { 2 })
        assertEquals(3, map.compute("a") { _, v -> (v ?: 0) + 2 })
        assertEquals(null, map.compute("a") { _, _ -> null })
        assertTrue(map.isEmpty())
    }

    @Test
    fun emailPatternIsPatternsCompats() {
        assertTrue(PatternsCompat.EMAIL_ADDRESS.matcher("ada@example.com").matches())
        assertFalse(PatternsCompat.EMAIL_ADDRESS.matcher("not an email").matches())
    }

    @Test
    fun characterHelpers() {
        assertEquals('f', Character.forDigit(15, 16))
        assertTrue(Character.isUnicodeIdentifierStart('a'))
        assertFalse(Character.isUnicodeIdentifierStart('1'))
        assertTrue(Character.isUnicodeIdentifierPart('1'))
    }
}
