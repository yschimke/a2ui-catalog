// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

import kotlin.math.abs
import kotlin.math.round

// A minimal en-US, UTC Gregorian implementation of the `java.text` members the port declares.
// Enough for every ISO pattern upstream parses and formats; not a general SimpleDateFormat.

private const val MILLIS_PER_DAY = 86_400_000L

private val MONTHS =
    listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December",
    )

private val WEEKDAYS =
    listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

private class Fields(
    var year: Int = 1970,
    var month: Int = 1,
    var day: Int = 1,
    var hour: Int = 0,
    var minute: Int = 0,
    var second: Int = 0,
    var millis: Int = 0,
) {
    fun toEpochMillis(): Long =
        daysFromCivil(year, month, day) * MILLIS_PER_DAY +
            hour * 3_600_000L + minute * 60_000L + second * 1_000L + millis

    companion object {
        fun of(epochMillis: Long): Fields {
            val days = Math.floorDiv(epochMillis, MILLIS_PER_DAY)
            var rem = epochMillis - days * MILLIS_PER_DAY
            val (y, m, d) = civilFromDays(days)
            val hour = (rem / 3_600_000L).toInt().also { rem -= it * 3_600_000L }
            val minute = (rem / 60_000L).toInt().also { rem -= it * 60_000L }
            val second = (rem / 1_000L).toInt().also { rem -= it * 1_000L }
            return Fields(y, m, d, hour, minute, second, rem.toInt())
        }
    }
}

private object Math {
    fun floorDiv(a: Long, b: Long): Long {
        val q = a / b
        return if ((a % b != 0L) && ((a < 0) != (b < 0))) q - 1 else q
    }
}

private fun civilFromDays(z0: Long): Triple<Int, Int, Int> {
    val z = z0 + 719_468
    val era = (if (z >= 0) z else z - 146_096) / 146_097
    val doe = z - era * 146_097
    val yoe = (doe - doe / 1_460 + doe / 36_524 - doe / 146_096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val m = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val y = (yoe + era * 400).toInt() + if (m <= 2) 1 else 0
    return Triple(y, m, d)
}

private fun daysFromCivil(y0: Int, m: Int, d: Int): Long {
    val y = (if (m <= 2) y0 - 1 else y0).toLong()
    val era = (if (y >= 0) y else y - 399) / 400
    val yoe = y - era * 400
    val doy = (153 * (if (m > 2) m - 3 else m + 9) + 2) / 5 + d - 1
    val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
    return era * 146_097 + doe - 719_468
}

private fun isLeap(y: Int) = (y % 4 == 0 && y % 100 != 0) || y % 400 == 0

private fun daysInMonth(y: Int, m: Int) =
    when (m) {
        2 -> if (isLeap(y)) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

/** A pattern letter run (`yyyy`) or a literal. */
private sealed interface Token {
    data class Field(val letter: Char, val count: Int) : Token

    data class Literal(val text: String) : Token
}

private fun tokenize(pattern: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    while (i < pattern.length) {
        val c = pattern[i]
        when {
            c == '\'' -> {
                val sb = StringBuilder()
                i++
                if (i < pattern.length && pattern[i] == '\'') {
                    tokens += Token.Literal("'")
                    i++
                    continue
                }
                while (i < pattern.length) {
                    if (pattern[i] == '\'') {
                        if (i + 1 < pattern.length && pattern[i + 1] == '\'') {
                            sb.append('\'')
                            i += 2
                            continue
                        }
                        break
                    }
                    sb.append(pattern[i++])
                }
                require(i < pattern.length) { "Unterminated quote in pattern: $pattern" }
                i++
                tokens += Token.Literal(sb.toString())
            }
            c in 'a'..'z' || c in 'A'..'Z' -> {
                var n = 1
                while (i + n < pattern.length && pattern[i + n] == c) n++
                require(c in "yMdHhmsSaEXZ") { "Unsupported pattern letter '$c' in: $pattern" }
                tokens += Token.Field(c, n)
                i += n
            }
            else -> {
                tokens += Token.Literal(c.toString())
                i++
            }
        }
    }
    return tokens
}

private fun Int.pad(n: Int) = toString().padStart(n, '0')

private class PatternEngine(pattern: String) : DateFormatEngine {
    private val tokens = tokenize(pattern)
    override var timeZoneId: String = "UTC"
    override var isLenient: Boolean = true

    override fun format(epochMillis: Long): String {
        val f = Fields.of(epochMillis)
        val sb = StringBuilder()
        for (t in tokens) {
            when (t) {
                is Token.Literal -> sb.append(t.text)
                is Token.Field ->
                    sb.append(
                        when (t.letter) {
                            'y' -> if (t.count == 2) (f.year % 100).pad(2) else f.year.pad(t.count)
                            'M' ->
                                when {
                                    t.count >= 4 -> MONTHS[f.month - 1]
                                    t.count == 3 -> MONTHS[f.month - 1].take(3)
                                    else -> f.month.pad(t.count)
                                }
                            'd' -> f.day.pad(t.count)
                            'H' -> f.hour.pad(t.count)
                            'h' -> (if (f.hour % 12 == 0) 12 else f.hour % 12).pad(t.count)
                            'm' -> f.minute.pad(t.count)
                            's' -> f.second.pad(t.count)
                            'S' -> f.millis.pad(t.count)
                            'a' -> if (f.hour < 12) "AM" else "PM"
                            'E' -> {
                                val dow = Math.floorDiv(epochMillis, MILLIS_PER_DAY).let {
                                    ((it + 3) % 7 + 7) % 7
                                }
                                WEEKDAYS[dow.toInt()].let { if (t.count >= 4) it else it.take(3) }
                            }
                            'X' -> "Z"
                            else -> "+0000"
                        }
                    )
            }
        }
        return sb.toString()
    }

    override fun parse(source: String): Long {
        val f = Fields()
        var pos = 0
        var offsetMinutes = 0
        var pm: Boolean? = null
        fun fail(): Nothing = throw ParseException("Unparseable date: \"$source\"", pos)
        for ((index, t) in tokens.withIndex()) {
            when (t) {
                is Token.Literal -> {
                    if (!source.startsWith(t.text, pos)) fail()
                    pos += t.text.length
                }
                is Token.Field -> {
                    when (t.letter) {
                        'X', 'Z' -> {
                            if (pos < source.length && (source[pos] == 'Z' || source[pos] == 'z')) {
                                pos++
                            } else if (
                                pos < source.length && (source[pos] == '+' || source[pos] == '-')
                            ) {
                                val sign = if (source[pos] == '-') -1 else 1
                                pos++
                                val hh = source.substring(pos).takeWhile { it.isDigit() }.take(2)
                                if (hh.length != 2) fail()
                                pos += 2
                                if (pos < source.length && source[pos] == ':') pos++
                                val mm = source.substring(pos).takeWhile { it.isDigit() }.take(2)
                                pos += mm.length
                                offsetMinutes =
                                    sign * (hh.toInt() * 60 + (mm.toIntOrNull() ?: 0))
                            } else {
                                fail()
                            }
                        }
                        'a' -> {
                            when {
                                source.startsWith("AM", pos, ignoreCase = true) -> pm = false
                                source.startsWith("PM", pos, ignoreCase = true) -> pm = true
                                else -> fail()
                            }
                            pos += 2
                        }
                        'E' -> fail()
                        'M' ->
                            if (t.count >= 3) {
                                val i =
                                    MONTHS.indexOfFirst {
                                        source.startsWith(
                                            if (t.count >= 4) it else it.take(3),
                                            pos,
                                            ignoreCase = true,
                                        )
                                    }
                                if (i < 0) fail()
                                pos += if (t.count >= 4) MONTHS[i].length else 3
                                f.month = i + 1
                            } else {
                                f.month = readNumber(source, pos, t, index) { pos = it } ?: fail()
                            }
                        else -> {
                            val n = readNumber(source, pos, t, index) { pos = it } ?: fail()
                            when (t.letter) {
                                'y' -> f.year = n
                                'd' -> f.day = n
                                'H' -> f.hour = n
                                'h' -> f.hour = n % 12
                                'm' -> f.minute = n
                                's' -> f.second = n
                                'S' -> f.millis = n
                            }
                        }
                    }
                }
            }
        }
        if (pm == true) f.hour += 12
        if (!isLenient) {
            if (f.month !in 1..12 || f.day !in 1..daysInMonth(f.year, f.month)) fail()
            if (f.hour !in 0..23 || f.minute !in 0..59 || f.second !in 0..59) fail()
            if (f.millis !in 0..999) fail()
        }
        return f.toEpochMillis() - offsetMinutes * 60_000L
    }

    /** Digits for [t]: exactly `count` when the next token is also numeric, else greedily. */
    private inline fun readNumber(
        source: String,
        start: Int,
        t: Token.Field,
        index: Int,
        setPos: (Int) -> Unit,
    ): Int? {
        val next = tokens.getOrNull(index + 1)
        val adjacentNumeric = next is Token.Field && next.letter in "yMdHhmsS"
        val max = if (adjacentNumeric) t.count else 9
        var end = start
        while (end < source.length && end - start < max && source[end].isDigit()) end++
        if (end == start) return null
        setPos(end)
        return source.substring(start, end).toInt()
    }
}

internal actual fun platformSimpleDateFormat(pattern: String, locale: Locale): DateFormatEngine =
    PatternEngine(pattern)

internal actual fun platformDateInstance(style: Int, locale: Locale): DateFormatEngine =
    PatternEngine(
        when (style) {
            DateFormat.SHORT -> "M/d/yy"
            DateFormat.MEDIUM -> "MMM d, yyyy"
            DateFormat.LONG -> "MMMM d, yyyy"
            else -> "EEEE, MMMM d, yyyy"
        }
    )

internal actual fun platformTimeInstance(style: Int, locale: Locale): DateFormatEngine =
    PatternEngine(if (style == DateFormat.SHORT) "h:mm a" else "h:mm:ss a")

private class UtcCalendar : CalendarEngine {
    private var fields = Fields.of(0)

    override var timeInMillis: Long
        get() = fields.toEpochMillis()
        set(value) {
            fields = Fields.of(value)
        }

    override fun get(field: Int): Int =
        when (field) {
            Calendar.YEAR -> fields.year
            Calendar.MONTH -> fields.month - 1
            Calendar.DAY_OF_MONTH -> fields.day
            Calendar.HOUR_OF_DAY -> fields.hour
            Calendar.MINUTE -> fields.minute
            Calendar.SECOND -> fields.second
            Calendar.MILLISECOND -> fields.millis
            else -> throw IllegalArgumentException("Unsupported Calendar field $field")
        }

    override fun set(field: Int, value: Int) {
        when (field) {
            Calendar.YEAR -> fields.year = value
            Calendar.MONTH -> fields.month = value + 1
            Calendar.DAY_OF_MONTH -> fields.day = value
            Calendar.HOUR_OF_DAY -> fields.hour = value
            Calendar.MINUTE -> fields.minute = value
            Calendar.SECOND -> fields.second = value
            Calendar.MILLISECOND -> fields.millis = value
            else -> throw IllegalArgumentException("Unsupported Calendar field $field")
        }
    }

    override fun clear() {
        fields = Fields.of(0)
    }
}

internal actual fun platformCalendar(timeZoneId: String): CalendarEngine =
    UtcCalendar().apply { timeInMillis = platformCurrentTimeMillis() }

private val CURRENCY_SYMBOLS = mapOf("USD" to "$", "EUR" to "€", "GBP" to "£", "JPY" to "¥")

private val ZERO_DECIMAL_CURRENCIES = setOf("JPY", "KRW", "VND", "CLP", "ISK")

internal actual fun platformCheckCurrency(currencyCode: String): String {
    require(currencyCode.length == 3 && currencyCode.all { it in 'A'..'Z' }) {
        "Invalid currency code: $currencyCode"
    }
    return currencyCode
}

private class DecimalEngine(private val isCurrency: Boolean) : NumberFormatEngine {
    override var isGroupingUsed: Boolean = true
    override var minimumFractionDigits: Int = if (isCurrency) 2 else 0
    override var maximumFractionDigits: Int = if (isCurrency) 2 else 3
    private var currencyCode = "USD"

    override fun setCurrency(currencyCode: String) {
        this.currencyCode = currencyCode
        if (isCurrency) {
            val digits = if (currencyCode in ZERO_DECIMAL_CURRENCIES) 0 else 2
            minimumFractionDigits = digits
            maximumFractionDigits = digits
        }
    }

    override fun format(number: Double): String {
        if (number.isNaN()) return "NaN"
        val negative = number < 0 || (number == 0.0 && 1 / number < 0)
        val body =
            if (number.isInfinite()) "∞" else digits(abs(number))
        val prefix = if (isCurrency) CURRENCY_SYMBOLS[currencyCode] ?: "$currencyCode " else ""
        return (if (negative) "-" else "") + prefix + body
    }

    private fun digits(value: Double): String {
        var scale = 1.0
        repeat(maximumFractionDigits) { scale *= 10 }
        val scaled = round(value * scale)
        if (scaled >= 9.0e18) return value.toString()
        val units = scaled.toLong()
        val divisor = scale.toLong()
        val integer = (units / divisor).toString()
        var fraction =
            if (maximumFractionDigits > 0) {
                (units % divisor).toString().padStart(maximumFractionDigits, '0')
            } else ""
        while (fraction.length > minimumFractionDigits && fraction.endsWith('0')) {
            fraction = fraction.dropLast(1)
        }
        val grouped =
            if (isGroupingUsed) integer.reversed().chunked(3).joinToString(",").reversed()
            else integer
        return if (fraction.isEmpty()) grouped else "$grouped.$fraction"
    }
}

internal actual fun platformNumberInstance(locale: Locale): NumberFormatEngine =
    DecimalEngine(isCurrency = false)

internal actual fun platformCurrencyInstance(locale: Locale): NumberFormatEngine =
    DecimalEngine(isCurrency = true)
