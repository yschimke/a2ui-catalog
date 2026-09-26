// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

/** `java.util.Currency`, identified by its ISO 4217 code. */
public class Currency private constructor(public val currencyCode: String) {
    public companion object {
        /** Throws [IllegalArgumentException] for a code that is not a known ISO 4217 currency. */
        public fun getInstance(currencyCode: String): Currency =
            Currency(platformCheckCurrency(currencyCode))
    }
}

/** `java.text.NumberFormat`, reduced to the members upstream sets and calls. */
public class NumberFormat private constructor(private val engine: NumberFormatEngine) {
    public var isGroupingUsed: Boolean
        get() = engine.isGroupingUsed
        set(value) {
            engine.isGroupingUsed = value
        }

    public var currency: Currency? = null
        set(value) {
            field = value
            if (value != null) engine.setCurrency(value.currencyCode)
        }

    public var minimumFractionDigits: Int
        get() = engine.minimumFractionDigits
        set(value) {
            engine.minimumFractionDigits = value
        }

    public var maximumFractionDigits: Int
        get() = engine.maximumFractionDigits
        set(value) {
            engine.maximumFractionDigits = value
        }

    public fun format(number: Double): String = engine.format(number)

    public companion object {
        public fun getNumberInstance(locale: Locale): NumberFormat =
            NumberFormat(platformNumberInstance(locale))

        public fun getCurrencyInstance(locale: Locale): NumberFormat =
            NumberFormat(platformCurrencyInstance(locale))
    }
}

internal interface NumberFormatEngine {
    var isGroupingUsed: Boolean
    var minimumFractionDigits: Int
    var maximumFractionDigits: Int

    fun setCurrency(currencyCode: String)

    fun format(number: Double): String
}

internal expect fun platformCheckCurrency(currencyCode: String): String

internal expect fun platformNumberInstance(locale: Locale): NumberFormatEngine

internal expect fun platformCurrencyInstance(locale: Locale): NumberFormatEngine
