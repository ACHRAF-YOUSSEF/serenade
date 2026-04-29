package com.serenade.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    themeChoice: SerenadeThemeChoice = SerenadeThemeChoice.Midnight,
    content: @Composable () -> Unit,
) {
    val sr = colorsFor(themeChoice)
    SrPaletteState.colors = sr
    val colorScheme = darkColorScheme(
        primary = sr.primary,
        onPrimary = sr.onPrimary,
        primaryContainer = sr.plum,
        onPrimaryContainer = sr.text,
        secondary = sr.coral,
        onSecondary = sr.text,
        secondaryContainer = sr.indigo,
        onSecondaryContainer = sr.text,
        tertiary = sr.amber,
        onTertiary = sr.onPrimary,
        tertiaryContainer = sr.surface,
        onTertiaryContainer = sr.textDim,
        background = sr.bg,
        onBackground = sr.text,
        surface = sr.surface,
        onSurface = sr.text,
        surfaceVariant = sr.surfaceHi,
        onSurfaceVariant = sr.textDim,
        surfaceTint = sr.plum,
        outline = sr.textMute,
        outlineVariant = sr.textOff,
        error = sr.coral,
        onError = sr.onPrimary,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
