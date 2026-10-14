package com.civicai.app.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.civicai.app.data.local.ThemePreset
import com.civicai.app.data.local.UserSettings

data class CivicExtraColors(
    val bg: Color,
    val card: Color,
    val box: Color,
    val input: Color,
    val border: Color,
    val borderLight: Color,
    val accent: Color,
    val accentLight: Color,
    val cyan: Color,
    val emerald: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val citationHighlight: Color,
    val isDark: Boolean
) {
    val hairline: Color get() = border
    val surface: Color get() = card
    val surfaceElevated: Color get() = box
    val groundedSuccess: Color get() = emerald
    val groundedWarning: Color get() = Color(0xFFD97706)
}

val LocalCivicColors = staticCompositionLocalOf {
    CivicExtraColors(
        bg = CivicColors.DarkCanvas,
        card = CivicColors.DarkCard,
        box = CivicColors.DarkBox,
        input = CivicColors.DarkInput,
        border = CivicColors.DarkBorder,
        borderLight = CivicColors.DarkBorderLight,
        accent = CivicColors.DarkAccent,
        accentLight = CivicColors.DarkAccentLight,
        cyan = CivicColors.DarkCyan,
        emerald = CivicColors.DarkEmerald,
        textPrimary = CivicColors.DarkTextPrimary,
        textSecondary = CivicColors.DarkTextSecondary,
        citationHighlight = Color(0xFF1E3A8A),
        isDark = true
    )
}

@Composable
fun CivicAITheme(
    settings: UserSettings = UserSettings(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()

    val isDark = when {
        settings.followSystemTheme -> systemDark
        else -> settings.isDarkMode
    }

    val parsedAccent = try {
        val cleanHex = settings.accentColorHex.removePrefix("#")
        Color(android.graphics.Color.parseColor("#$cleanHex"))
    } catch (e: Exception) {
        if (isDark) CivicColors.DarkAccent else CivicColors.LightTeal
    }

    val rawColors = if (isDark) {
        CivicExtraColors(
            bg = CivicColors.DarkCanvas,
            card = CivicColors.DarkCard,
            box = CivicColors.DarkBox,
            input = CivicColors.DarkInput,
            border = CivicColors.DarkBorder,
            borderLight = CivicColors.DarkBorderLight,
            accent = parsedAccent,
            accentLight = CivicColors.DarkAccentLight,
            cyan = CivicColors.DarkCyan,
            emerald = CivicColors.DarkEmerald,
            textPrimary = CivicColors.DarkTextPrimary,
            textSecondary = CivicColors.DarkTextSecondary,
            citationHighlight = Color(0xFF1E293B),
            isDark = true
        )
    } else {
        CivicExtraColors(
            bg = CivicColors.LightCanvas,
            card = CivicColors.LightCard,
            box = CivicColors.LightSubtle,
            input = CivicColors.LightInput,
            border = CivicColors.LightBorder,
            borderLight = CivicColors.LightBorderLight,
            accent = if (settings.accentColorHex == "#2563EB") CivicColors.LightTeal else parsedAccent,
            accentLight = CivicColors.LightNavy,
            cyan = CivicColors.LightTeal,
            emerald = CivicColors.LightEmerald,
            textPrimary = CivicColors.LightTextPrimary,
            textSecondary = CivicColors.LightTextSecondary,
            citationHighlight = CivicColors.LightCitationHighlight,
            isDark = false
        )
    }

    // Animated Smooth Cross-fade between themes
    val animDuration = if (settings.reduceMotion) 0 else 300
    val animSpec = tween<Color>(durationMillis = animDuration)

    val animatedBg = animateColorAsState(rawColors.bg, animSpec, label = "bg").value
    val animatedCard = animateColorAsState(rawColors.card, animSpec, label = "card").value
    val animatedBox = animateColorAsState(rawColors.box, animSpec, label = "box").value
    val animatedInput = animateColorAsState(rawColors.input, animSpec, label = "input").value
    val animatedBorder = animateColorAsState(rawColors.border, animSpec, label = "border").value
    val animatedBorderLight = animateColorAsState(rawColors.borderLight, animSpec, label = "borderLight").value
    val animatedAccent = animateColorAsState(rawColors.accent, animSpec, label = "accent").value
    val animatedAccentLight = animateColorAsState(rawColors.accentLight, animSpec, label = "accentLight").value
    val animatedTextPrimary = animateColorAsState(rawColors.textPrimary, animSpec, label = "textP").value
    val animatedTextSecondary = animateColorAsState(rawColors.textSecondary, animSpec, label = "textS").value
    val animatedCyan = animateColorAsState(rawColors.cyan, animSpec, label = "cyan").value
    val animatedEmerald = animateColorAsState(rawColors.emerald, animSpec, label = "emerald").value

    val animatedCivicColors = CivicExtraColors(
        bg = animatedBg,
        card = animatedCard,
        box = animatedBox,
        input = animatedInput,
        border = animatedBorder,
        borderLight = animatedBorderLight,
        accent = animatedAccent,
        accentLight = animatedAccentLight,
        cyan = animatedCyan,
        emerald = animatedEmerald,
        textPrimary = animatedTextPrimary,
        textSecondary = animatedTextSecondary,
        citationHighlight = rawColors.citationHighlight,
        isDark = isDark
    )

    val materialColorScheme = if (isDark) {
        darkColorScheme(
            primary = animatedAccent,
            background = animatedBg,
            surface = animatedCard,
            onBackground = animatedTextPrimary,
            onSurface = animatedTextPrimary,
            surfaceContainer = animatedBox,
            outline = animatedBorder
        )
    } else {
        lightColorScheme(
            primary = animatedAccent,
            background = animatedBg,
            surface = animatedCard,
            onBackground = animatedTextPrimary,
            onSurface = animatedTextPrimary,
            surfaceContainer = animatedBox,
            outline = animatedBorder
        )
    }

    val scaledTypography = getScaledTypography(settings.fontSizeScale)

    CompositionLocalProvider(LocalCivicColors provides animatedCivicColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = scaledTypography,
            content = content
        )
    }
}

val MaterialTheme.civicColors: CivicExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCivicColors.current
