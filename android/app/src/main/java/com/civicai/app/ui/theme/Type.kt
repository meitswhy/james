package com.civicai.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

// Typography System for Quiet Luxury Civic Tech
val DisplayFontFamily = FontFamily.SansSerif
val BodyFontFamily = FontFamily.SansSerif
val MonospaceFontFamily = FontFamily.Monospace

private val BaselineLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None
)

fun getScaledTypography(fontSizeScale: Float): Typography {
    val scale = fontSizeScale.coerceIn(0.85f, 1.30f)

    return Typography(
        displayLarge = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * scale).sp,
            lineHeight = (38 * scale).sp,
            letterSpacing = (-0.8).sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        displayMedium = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (26 * scale).sp,
            lineHeight = (32 * scale).sp,
            letterSpacing = (-0.5).sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        headlineMedium = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = (20 * scale).sp,
            lineHeight = (26 * scale).sp,
            letterSpacing = (-0.3).sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        titleLarge = TextStyle(
            fontFamily = DisplayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (17 * scale).sp,
            lineHeight = (23 * scale).sp,
            letterSpacing = (-0.2).sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        titleMedium = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (15 * scale).sp,
            lineHeight = (21 * scale).sp,
            letterSpacing = 0.sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        bodyLarge = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (15 * scale).sp,
            lineHeight = (22 * scale).sp,
            letterSpacing = 0.15.sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        bodyMedium = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (13.5 * scale).sp,
            lineHeight = (19 * scale).sp,
            letterSpacing = 0.1.sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        labelLarge = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (13 * scale).sp,
            lineHeight = (18 * scale).sp,
            letterSpacing = 0.1.sp,
            lineHeightStyle = BaselineLineHeightStyle
        ),
        labelSmall = TextStyle(
            fontFamily = BodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * scale).sp,
            lineHeight = (15 * scale).sp,
            letterSpacing = 0.sp,
            lineHeightStyle = BaselineLineHeightStyle
        )
    )
}
