package com.serenade.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SerenadeColorScheme = darkColorScheme(
    primary             = SrPrimary,
    onPrimary           = SrOnPrimary,
    primaryContainer    = SrPlum,
    onPrimaryContainer  = SrText,
    secondary           = SrCoral,
    onSecondary         = SrText,
    secondaryContainer  = SrIndigo,
    onSecondaryContainer = SrText,
    tertiary            = SrAmber,
    onTertiary          = SrOnPrimary,
    tertiaryContainer   = SrSurface,
    onTertiaryContainer = SrTextDim,
    background          = SrBg,
    onBackground        = SrText,
    surface             = SrSurface,
    onSurface           = SrText,
    surfaceVariant      = SrSurfaceHi,
    onSurfaceVariant    = SrTextDim,
    surfaceTint         = SrPlum,
    outline             = SrTextMute,
    outlineVariant      = SrTextOff,
    error               = SrCoral,
    onError             = SrOnPrimary,
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SerenadeColorScheme,
        typography = Typography,
        content = content,
    )
}
