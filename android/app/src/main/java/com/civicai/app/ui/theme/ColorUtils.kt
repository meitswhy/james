package com.civicai.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ColorUtils {

    fun calculateLuminance(color: Color): Double {
        val argb = color.toArgb()
        val r = ((argb shr 16) and 0xFF) / 255.0
        val g = ((argb shr 8) and 0xFF) / 255.0
        val b = (argb and 0xFF) / 255.0

        val rL = if (r <= 0.04045) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)
        val gL = if (g <= 0.04045) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)
        val bL = if (b <= 0.04045) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)

        return 0.2126 * rL + 0.7152 * gL + 0.0722 * bL
    }

    fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)
    }

    fun passesWcagAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val ratio = calculateContrastRatio(foreground, background)
        val requiredRatio = if (isLargeText) 3.0 else 4.5
        return ratio >= requiredRatio
    }
}
