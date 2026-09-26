// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

/**
 * The locale type upstream's public API names (`A2uiLocaleProvider.getLocale()`,
 * `A2uiMessageFormatter.format`). On the JVM it IS `java.util.Locale` — an `actual typealias` — so
 * the port's JVM signatures are binary-identical to the AndroidX artifacts'.
 */
public expect class Locale

/**
 * `Locale.US` and `Locale.getDefault()`. An `expect class` cannot declare a companion that a Java
 * class's statics satisfy, so these two call sites are the only ones that change their receiver.
 */
public object Locales {
    public val US: Locale
        get() = platformLocaleUs()

    public fun getDefault(): Locale = platformDefaultLocale()

    /** `Locale.forLanguageTag`, for a Compose `Locale` whose platform locale is not common API. */
    public fun forLanguageTag(languageTag: String): Locale = platformLocaleForTag(languageTag)
}

internal expect fun platformLocaleForTag(languageTag: String): Locale

internal expect fun platformLocaleUs(): Locale

internal expect fun platformDefaultLocale(): Locale

/**
 * `android.text.format.DateFormat.is24HourFormat(Context)`, without the `Context`: whether the
 * default locale's short time format uses a 24-hour clock.
 */
public expect fun is24HourFormat(): Boolean
