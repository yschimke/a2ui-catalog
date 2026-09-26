// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

// `java.util.Date` / `TimeZone` / `Calendar` and `java.text.DateFormat` / `SimpleDateFormat` /
// `ParseException`, reduced to the members upstream calls. None of them appears in upstream's
// public API, so these are plain common classes over a per-platform engine: on the JVM the engine
// IS the `java.text` / `java.util` class, so formatting and parsing behave exactly as upstream's.

/** `java.util.Date`: an instant in epoch milliseconds. */
public class Date(public val time: Long)

/** `java.util.TimeZone`, identified by its id. */
public class TimeZone private constructor(public val id: String) {
    public companion object {
        public fun getTimeZone(id: String): TimeZone = TimeZone(id)
    }
}

/** `java.text.ParseException`. */
public open class ParseException(message: String?, public val errorOffset: Int) :
    Exception(message)

/** `java.text.DateFormat`. */
public open class DateFormat internal constructor(private val engine: DateFormatEngine) {
    public var timeZone: TimeZone = TimeZone.getTimeZone(engine.timeZoneId)
        set(value) {
            field = value
            engine.timeZoneId = value.id
        }

    public var isLenient: Boolean
        get() = engine.isLenient
        set(value) {
            engine.isLenient = value
        }

    public fun format(date: Date): String = engine.format(date.time)

    /** Parses a leading date from [source]; throws [ParseException] when there is none. */
    public fun parse(source: String): Date? = Date(engine.parse(source))

    public companion object {
        public const val FULL: Int = 0
        public const val LONG: Int = 1
        public const val MEDIUM: Int = 2
        public const val SHORT: Int = 3

        public fun getDateInstance(style: Int, locale: Locale): DateFormat =
            DateFormat(platformDateInstance(style, locale))

        public fun getTimeInstance(style: Int, locale: Locale): DateFormat =
            DateFormat(platformTimeInstance(style, locale))
    }
}

/** `java.text.SimpleDateFormat`. */
public class SimpleDateFormat(pattern: String, locale: Locale) :
    DateFormat(platformSimpleDateFormat(pattern, locale))

/** `java.util.Calendar`, Gregorian, reduced to the fields upstream reads and writes. */
public class Calendar private constructor(private val engine: CalendarEngine) {
    public var timeInMillis: Long
        get() = engine.timeInMillis
        set(value) {
            engine.timeInMillis = value
        }

    public fun get(field: Int): Int = engine.get(field)

    public fun set(field: Int, value: Int) {
        engine.set(field, value)
    }

    public fun clear() {
        engine.clear()
    }

    public companion object {
        // The `java.util.Calendar` field numbers, so an engine can pass them straight through.
        public const val YEAR: Int = 1
        public const val MONTH: Int = 2
        public const val DAY_OF_MONTH: Int = 5
        public const val HOUR_OF_DAY: Int = 11
        public const val MINUTE: Int = 12
        public const val SECOND: Int = 13
        public const val MILLISECOND: Int = 14

        public fun getInstance(zone: TimeZone): Calendar = Calendar(platformCalendar(zone.id))
    }
}

internal interface DateFormatEngine {
    var timeZoneId: String
    var isLenient: Boolean

    fun format(epochMillis: Long): String

    /** Throws [ParseException] when [source] does not start with a date in this format. */
    fun parse(source: String): Long
}

internal interface CalendarEngine {
    var timeInMillis: Long

    fun get(field: Int): Int

    fun set(field: Int, value: Int)

    fun clear()
}

internal expect fun platformSimpleDateFormat(pattern: String, locale: Locale): DateFormatEngine

internal expect fun platformDateInstance(style: Int, locale: Locale): DateFormatEngine

internal expect fun platformTimeInstance(style: Int, locale: Locale): DateFormatEngine

internal expect fun platformCalendar(timeZoneId: String): CalendarEngine
