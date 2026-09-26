// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

// The wasmJs half of the port seam. The browser is single-threaded, so the concurrency types are
// plain; the `java.text` stand-ins are a small, en-US, UTC-only Gregorian implementation (see
// WasmTime.kt), which is what every call site upstream uses them for except the two localized ones
// (`formatDate` with a caller's pattern and `DateTimeInput`'s chip label). See vendor/README.md.

public actual class Locale(public val languageTag: String) {
    override fun toString(): String = languageTag

    override fun equals(other: Any?): Boolean = other is Locale && other.languageTag == languageTag

    override fun hashCode(): Int = languageTag.hashCode()
}

private val US_LOCALE = Locale("en-US")

internal actual fun platformLocaleUs(): Locale = US_LOCALE

internal actual fun platformDefaultLocale(): Locale = US_LOCALE

internal actual fun platformLocaleForTag(languageTag: String): Locale = Locale(languageTag)

public actual fun is24HourFormat(): Boolean = false

private fun dateNow(): Double = js("Date.now()")

internal actual fun platformCurrentTimeMillis(): Long = dateNow().toLong()

internal actual fun platformIdentityHashCode(x: Any?): Int = x?.hashCode() ?: 0

internal actual fun <K : Any, V : Any> platformConcurrentMap(): MutableMap<K, V> = LinkedHashMap()

internal actual fun <K : Any, V : Any> MutableMap<K, V>.platformComputeIfAbsent(
    key: K,
    mappingFunction: (K) -> V,
): V = getOrPut(key) { mappingFunction(key) }

internal actual fun <K : Any, V : Any> MutableMap<K, V>.platformCompute(
    key: K,
    remappingFunction: (K, V?) -> V?,
): V? {
    val result = remappingFunction(key, get(key))
    if (result == null) remove(key) else put(key, result)
    return result
}

public actual class AtomicInteger actual constructor(initialValue: Int) {
    private var value = initialValue

    public actual fun get(): Int = value

    public actual fun incrementAndGet(): Int = ++value

    public actual fun decrementAndGet(): Int = --value
}

public actual class ReentrantLock actual constructor(fair: Boolean) {
    public actual fun lock() {}

    public actual fun unlock() {}
}

// UAX #31 approximated with Kotlin's Unicode categories: ID_Start is letters and letter numbers,
// ID_Continue adds digits, combining marks and connector punctuation.
internal actual fun platformIsIdentifierStart(ch: Char): Boolean =
    ch.isLetter() || ch.category == CharCategory.LETTER_NUMBER

internal actual fun platformIsIdentifierPart(ch: Char): Boolean =
    platformIsIdentifierStart(ch) ||
        ch.isDigit() ||
        ch.category == CharCategory.NON_SPACING_MARK ||
        ch.category == CharCategory.COMBINING_SPACING_MARK ||
        ch.category == CharCategory.CONNECTOR_PUNCTUATION
