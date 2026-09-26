// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

// The JVM half of the port seam. Every actual here is the `java.*` class upstream named, so on the
// JVM the port formats, parses, locks and hashes exactly as the AndroidX artifacts do on Android.

public actual typealias Locale = java.util.Locale

internal actual fun platformLocaleUs(): Locale = java.util.Locale.US

internal actual fun platformDefaultLocale(): Locale = java.util.Locale.getDefault()

internal actual fun platformLocaleForTag(languageTag: String): Locale =
    java.util.Locale.forLanguageTag(languageTag)

public actual fun is24HourFormat(): Boolean {
    val format =
        java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, java.util.Locale.getDefault())
    return (format as? java.text.SimpleDateFormat)?.toPattern()?.contains('H') ?: false
}

internal actual fun platformCurrentTimeMillis(): Long = java.lang.System.currentTimeMillis()

internal actual fun platformIdentityHashCode(x: Any?): Int = java.lang.System.identityHashCode(x)

internal actual fun <K : Any, V : Any> platformConcurrentMap(): MutableMap<K, V> =
    java.util.concurrent.ConcurrentHashMap()

internal actual fun <K : Any, V : Any> MutableMap<K, V>.platformComputeIfAbsent(
    key: K,
    mappingFunction: (K) -> V,
): V = (this as java.util.concurrent.ConcurrentHashMap<K, V>).computeIfAbsent(key, mappingFunction)

internal actual fun <K : Any, V : Any> MutableMap<K, V>.platformCompute(
    key: K,
    remappingFunction: (K, V?) -> V?,
): V? = (this as java.util.concurrent.ConcurrentHashMap<K, V>).compute(key, remappingFunction)

public actual typealias AtomicInteger = java.util.concurrent.atomic.AtomicInteger

public actual typealias ReentrantLock = java.util.concurrent.locks.ReentrantLock

private class JavaDateFormatEngine(private val format: java.text.DateFormat) : DateFormatEngine {
    override var timeZoneId: String
        get() = format.timeZone.id
        set(value) {
            format.timeZone = java.util.TimeZone.getTimeZone(value)
        }

    override var isLenient: Boolean
        get() = format.isLenient
        set(value) {
            format.isLenient = value
        }

    override fun format(epochMillis: Long): String = format.format(java.util.Date(epochMillis))

    override fun parse(source: String): Long =
        try {
            format.parse(source).time
        } catch (e: java.text.ParseException) {
            throw ParseException(e.message, e.errorOffset)
        }
}

internal actual fun platformSimpleDateFormat(pattern: String, locale: Locale): DateFormatEngine =
    JavaDateFormatEngine(java.text.SimpleDateFormat(pattern, locale))

internal actual fun platformDateInstance(style: Int, locale: Locale): DateFormatEngine =
    JavaDateFormatEngine(java.text.DateFormat.getDateInstance(style, locale))

internal actual fun platformTimeInstance(style: Int, locale: Locale): DateFormatEngine =
    JavaDateFormatEngine(java.text.DateFormat.getTimeInstance(style, locale))

private class JavaCalendarEngine(private val calendar: java.util.Calendar) : CalendarEngine {
    override var timeInMillis: Long
        get() = calendar.timeInMillis
        set(value) {
            calendar.timeInMillis = value
        }

    override fun get(field: Int): Int = calendar.get(field)

    override fun set(field: Int, value: Int) {
        calendar.set(field, value)
    }

    override fun clear() {
        calendar.clear()
    }
}

internal actual fun platformCalendar(timeZoneId: String): CalendarEngine =
    JavaCalendarEngine(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone(timeZoneId)))

private class JavaNumberFormatEngine(private val format: java.text.NumberFormat) :
    NumberFormatEngine {
    override var isGroupingUsed: Boolean
        get() = format.isGroupingUsed
        set(value) {
            format.isGroupingUsed = value
        }

    override var minimumFractionDigits: Int
        get() = format.minimumFractionDigits
        set(value) {
            format.minimumFractionDigits = value
        }

    override var maximumFractionDigits: Int
        get() = format.maximumFractionDigits
        set(value) {
            format.maximumFractionDigits = value
        }

    override fun setCurrency(currencyCode: String) {
        format.currency = java.util.Currency.getInstance(currencyCode)
    }

    override fun format(number: Double): String = format.format(number)
}

internal actual fun platformCheckCurrency(currencyCode: String): String =
    java.util.Currency.getInstance(currencyCode).currencyCode

internal actual fun platformNumberInstance(locale: Locale): NumberFormatEngine =
    JavaNumberFormatEngine(java.text.NumberFormat.getNumberInstance(locale))

internal actual fun platformCurrencyInstance(locale: Locale): NumberFormatEngine =
    JavaNumberFormatEngine(java.text.NumberFormat.getCurrencyInstance(locale))

internal actual fun platformIsIdentifierStart(ch: Char): Boolean =
    java.lang.Character.isUnicodeIdentifierStart(ch)

internal actual fun platformIsIdentifierPart(ch: Char): Boolean =
    java.lang.Character.isUnicodeIdentifierPart(ch)
