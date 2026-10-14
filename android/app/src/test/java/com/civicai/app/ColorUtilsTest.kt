package com.civicai.app

import com.civicai.app.ui.theme.CivicColors
import com.civicai.app.ui.theme.ColorUtils
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorUtilsTest {

    @Test
    fun verifyDarkNavyThemeContrastPassesWcagAA() {
        val contrast = ColorUtils.calculateContrastRatio(
            CivicColors.DarkTextPrimary,
            CivicColors.DarkCanvas
        )
        assertTrue("Dark Navy theme text contrast ($contrast) must be >= 4.5", contrast >= 4.5)
    }

    @Test
    fun verifyCivicVanguardLightThemeContrastPassesWcagAA() {
        val contrast = ColorUtils.calculateContrastRatio(
            CivicColors.LightTextPrimary,
            CivicColors.LightCanvas
        )
        assertTrue("Civic Vanguard Light theme text contrast ($contrast) must be >= 4.5", contrast >= 4.5)
    }

    @Test
    fun verifyDarkNavyAccentContrast() {
        val contrast = ColorUtils.calculateContrastRatio(
            CivicColors.DarkAccent,
            CivicColors.DarkCanvas
        )
        assertTrue("Dark Navy accent contrast ($contrast) must be >= 3.0", contrast >= 3.0)
    }

    @Test
    fun verifyCivicVanguardLightTealContrast() {
        val contrast = ColorUtils.calculateContrastRatio(
            CivicColors.LightTeal,
            CivicColors.LightCanvas
        )
        assertTrue("Light Teal contrast ($contrast) must be >= 3.0", contrast >= 3.0)
    }
}
