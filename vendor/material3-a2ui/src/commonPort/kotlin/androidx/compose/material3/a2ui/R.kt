// SPDX-License-Identifier: Apache-2.0
// a2ui-catalog's Compose Multiplatform port seam — not AndroidX code. See vendor/README.md.

package androidx.compose.material3.a2ui

import androidx.compose.runtime.Composable

/**
 * Stands in for the Android `R` class upstream's `R.java` declares. Each key is upstream's
 * `res/values/strings.xml` name and each value its default (English) string, so the vendored call
 * sites — `stringResource(R.string.select_date)` — compile unchanged. Upstream's translations are
 * not carried: a desktop or browser surface draws the English defaults.
 */
internal object R {
    @Suppress("ClassName")
    object string {
        const val button_cancel: String = "Cancel"
        const val button_ok: String = "OK"
        const val error: String = "Error"
        const val select_date: String = "Select date"
        const val select_time: String = "Select time"
        const val choice_picker_placeholder_select_options: String = "Select options"
        const val choice_picker_placeholder_show_options: String = "Show options"
        const val choice_picker_placeholder_filter: String = "Start typing to filter…"
        const val choice_picker_no_matching_options: String = "No matching options"
        const val choice_picker_remove_option: String = "Remove %1\$s"
        const val choice_picker_clear_search: String = "Clear search filter"
    }
}

/** `androidx.compose.ui.res.stringResource` over [R]: the string, with `%n$s` args substituted. */
@Composable
internal fun stringResource(id: String, vararg formatArgs: Any): String {
    var result = id
    formatArgs.forEachIndexed { index, arg -> result = result.replace("%${index + 1}\$s", arg.toString()) }
    return result
}
