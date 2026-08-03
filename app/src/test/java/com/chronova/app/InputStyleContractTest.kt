package com.chronova.app

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Contract tests for the readable TextInputLayout styles added in
 * fix for issue #50 (unreadable dark-blue-on-grey inputs).
 *
 * The fix is a theme-level style override. These tests guard the
 * contract that:
 *   1. The override styles exist and pin colors to the readable palette.
 *   2. The app theme references them as the default text-input styles.
 *
 * If either contract breaks in a future refactor, the input readability
 * regression returns.
 */
class InputStyleContractTest {

    private fun readResource(relativePath: String): String {
        val file = File("src/main/res", relativePath)
        assertTrue("Expected resource at ${file.path}", file.exists())
        return file.readText()
    }

    @Test
    fun styles_xml_contains_readable_textinput_overrides() {
        val styles = readResource("values/styles.xml")

        // Style must exist and target the Material2 OutlinedBox the layouts use.
        assertTrue(
            "Chronova.TextInputLayout must extend Widget.MaterialComponents.TextInputLayout.OutlinedBox",
            styles.contains("name=\"Chronova.TextInputLayout\"") &&
                styles.contains("parent=\"Widget.MaterialComponents.TextInputLayout.OutlinedBox\"")
        )

        // Hint/stroke colors must be the readable palette, not colorPrimary.
        assertTrue(
            "boxStrokeColor must be @color/text_primary for legibility",
            styles.contains("boxStrokeColor") && styles.contains("@color/text_primary")
        )
        assertTrue(
            "hintTextColor must be @color/text_primary for legibility",
            styles.contains("hintTextColor") && styles.contains("@color/text_primary")
        )
        assertTrue(
            "android:textColorHint must be @color/text_secondary for legibility",
            styles.contains("android:textColorHint") && styles.contains("@color/text_secondary")
        )

        // EditText text must use the readable primary color.
        assertTrue(
            "Chronova.TextInputEditText must set android:textColor to @color/text_primary",
            styles.contains("name=\"Chronova.TextInputEditText\"") &&
                styles.contains("android:textColor") &&
                styles.contains("@color/text_primary")
        )
    }

    @Test
    fun theme_xml_wires_textinput_overrides_as_defaults() {
        val themes = readResource("values/themes.xml")

        // Theme.Chronova must point the framework defaults at the override styles
        // so every TextInputLayout / TextInputEditText in the app inherits readable colors.
        val themeBlock = Regex(
            "name=\"Theme\\.Chronova\".*?</style>",
            setOf(RegexOption.DOT_MATCHES_ALL)
        ).find(themes)?.value
        assertNotNull("Theme.Chronova block must exist in themes.xml", themeBlock)

        assertTrue(
            "Theme.Chronova must set textInputStyle to @style/Chronova.TextInputLayout",
            themeBlock!!.contains("textInputStyle") &&
                themeBlock.contains("@style/Chronova.TextInputLayout")
        )
        assertTrue(
            "Theme.Chronova must set editTextStyle to @style/Chronova.TextInputEditText",
            themeBlock.contains("editTextStyle") &&
                themeBlock.contains("@style/Chronova.TextInputEditText")
        )
    }
}
