// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package ee.schimke.a2uicmp.port

/**
 * `androidx.core.util.PatternsCompat`, which lives in an Android-only AAR. Only [EMAIL_ADDRESS] is
 * used upstream; its expression is copied verbatim from `PatternsCompat.java` (androidx-main).
 */
public object PatternsCompat {
    public val EMAIL_ADDRESS: Pattern =
        Pattern(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
        )
}

/** The `java.util.regex.Pattern` / `Matcher` pair `PatternsCompat` hands out, over [Regex]. */
public class Pattern(pattern: String) {
    private val regex = Regex(pattern)

    public fun matcher(input: CharSequence): Matcher = Matcher(regex, input)
}

public class Matcher internal constructor(private val regex: Regex, private val input: CharSequence) {
    public fun matches(): Boolean = regex.matches(input)
}
