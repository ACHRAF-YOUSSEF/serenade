package com.serenade.app.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class SerenadeThemeChoice(
    val storageKey: String,
    val label: String,
    val subtitle: String,
) {
    Midnight("midnight", "Midnight Velvet", "Candlelit plum and copper"),
    Aurora("aurora", "Aurora Pulse", "Teal, blue, and ultraviolet"),
}

data class SerenadeColors(
    val bg: Color,
    val bgDeep: Color,
    val surface: Color,
    val surfaceHi: Color,
    val line: Color,
    val lineHi: Color,
    val primary: Color,
    val primaryHi: Color,
    val plum: Color,
    val indigo: Color,
    val coral: Color,
    val amber: Color,
    val onPrimary: Color,
    val text: Color,
    val textDim: Color,
    val textMute: Color,
    val textOff: Color,
    val good: Color,
    val warn: Color,
)

private val MidnightColors = SerenadeColors(
    bg = Color(0xFF0B0612),
    bgDeep = Color(0xFF070410),
    surface = Color(0xFF160E22),
    surfaceHi = Color(0xFF1F1530),
    line = Color(0x12FFFFFF),
    lineHi = Color(0x24FFFFFF),
    primary = Color(0xFFE8B07A),
    primaryHi = Color(0xFFF4C492),
    plum = Color(0xFF6B3F8E),
    indigo = Color(0xFF3A2E6B),
    coral = Color(0xFFE26D6D),
    amber = Color(0xFFF2B547),
    onPrimary = Color(0xFF1A0D05),
    text = Color(0xFFF4ECDF),
    textDim = Color(0xFFB7A7C0),
    textMute = Color(0xFF7A6E86),
    textOff = Color(0xFF544A60),
    good = Color(0xFF7CC4A4),
    warn = Color(0xFFE8A05B),
)

private val AuroraColors = MidnightColors.copy(
    bg = Color(0xFF06121A),
    bgDeep = Color(0xFF020A12),
    surface = Color(0xFF0E2030),
    surfaceHi = Color(0xFF16304A),
    primary = Color(0xFF5EE6C7),
    primaryHi = Color(0xFF7BF1D5),
    plum = Color(0xFF3D6FB8),
    indigo = Color(0xFF1D3A6B),
    coral = Color(0xFFC76FE6),
    amber = Color(0xFFF2D14A),
    onPrimary = Color(0xFF001F1A),
    text = Color(0xFFE6F4FF),
    textDim = Color(0xFF9DB6CC),
    textMute = Color(0xFF5E7388),
    textOff = Color(0xFF3A4B5E),
)

internal fun colorsFor(choice: SerenadeThemeChoice): SerenadeColors =
    when (choice) {
        SerenadeThemeChoice.Midnight -> MidnightColors
        SerenadeThemeChoice.Aurora -> AuroraColors
    }

internal object SrPaletteState {
    var colors by mutableStateOf(MidnightColors)
}

val SrBg: Color get() = SrPaletteState.colors.bg
val SrBgDeep: Color get() = SrPaletteState.colors.bgDeep
val SrSurface: Color get() = SrPaletteState.colors.surface
val SrSurfaceHi: Color get() = SrPaletteState.colors.surfaceHi
val SrLine: Color get() = SrPaletteState.colors.line
val SrLineHi: Color get() = SrPaletteState.colors.lineHi
val SrPrimary: Color get() = SrPaletteState.colors.primary
val SrPrimaryHi: Color get() = SrPaletteState.colors.primaryHi
val SrPlum: Color get() = SrPaletteState.colors.plum
val SrIndigo: Color get() = SrPaletteState.colors.indigo
val SrCoral: Color get() = SrPaletteState.colors.coral
val SrAmber: Color get() = SrPaletteState.colors.amber
val SrOnPrimary: Color get() = SrPaletteState.colors.onPrimary
val SrText: Color get() = SrPaletteState.colors.text
val SrTextDim: Color get() = SrPaletteState.colors.textDim
val SrTextMute: Color get() = SrPaletteState.colors.textMute
val SrTextOff: Color get() = SrPaletteState.colors.textOff
val SrGood: Color get() = SrPaletteState.colors.good
val SrWarn: Color get() = SrPaletteState.colors.warn
