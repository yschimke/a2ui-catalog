// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

/**
 * The two `java.lang.System` members upstream calls. Imported explicitly, it shadows the implicit
 * `java.lang.System` on the JVM, so `System.currentTimeMillis()` compiles unchanged in common code.
 */
public object System {
    public fun currentTimeMillis(): Long = platformCurrentTimeMillis()

    public fun identityHashCode(x: Any?): Int = platformIdentityHashCode(x)
}

internal expect fun platformCurrentTimeMillis(): Long

internal expect fun platformIdentityHashCode(x: Any?): Int

/**
 * The `java.lang.Character` members upstream calls (UAX #31 identifier checks, `forDigit`).
 * Imported explicitly, it shadows the implicit `java.lang.Character` on the JVM.
 */
public object Character {
    /** `Character.forDigit`: the lower-case digit character, or `'\u0000'` when out of range. */
    public fun forDigit(digit: Int, radix: Int): Char =
        when {
            radix !in 2..36 || digit !in 0 until radix -> '\u0000'
            digit < 10 -> '0' + digit
            else -> 'a' + (digit - 10)
        }

    public fun isUnicodeIdentifierStart(ch: Char): Boolean = platformIsIdentifierStart(ch)

    public fun isUnicodeIdentifierPart(ch: Char): Boolean = platformIsIdentifierPart(ch)
}

internal expect fun platformIsIdentifierStart(ch: Char): Boolean

internal expect fun platformIsIdentifierPart(ch: Char): Boolean
