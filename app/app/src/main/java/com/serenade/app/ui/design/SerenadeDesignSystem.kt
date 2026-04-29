package com.serenade.app.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.serenade.app.ui.theme.*

// ── ArtworkAvatar ────────────────────────────────────────────
// Generative gradient + monogram — stable per seed.
@Composable
fun ArtworkAvatar(
    seed: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 8.dp,
    isCircle: Boolean = false,
) {
    val (c1, c2, angle, initials) = remember(seed) { artworkParams(seed) }
    val shape = if (isCircle) CircleShape else RoundedCornerShape(cornerRadius)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(c1, c2),
                    start = Offset(0f, 0f),
                    end = Offset(1f, 1f),
                )
            ),
    ) {
        // Vinyl ring motif
        Box(
            modifier = Modifier
                .size(size * 0.72f)
                .clip(CircleShape)
                .background(Color.Transparent),
        )
        Text(
            text = initials,
            color = Color.White.copy(alpha = 0.85f),
            fontSize = (size.value * 0.32f).sp,
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Italic,
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = (size.value * 0.32f).sp,
            ),
        )
    }
}

private data class ArtParams(val c1: Color, val c2: Color, val angle: Float, val initials: String)

private fun artworkParams(seed: String): ArtParams {
    var h = 2166136261.toInt()
    for (ch in seed) {
        h = h xor ch.code
        h = (h.toLong() * 16777619L).toInt()
    }
    val hue1 = (Math.abs(h) % 360).toFloat()
    val hue2 = ((hue1 + 40f + (Math.abs(h shr 8) % 60)) % 360f)
    val c1 = Color.hsl(hue1, 0.45f, 0.28f)
    val c2 = Color.hsl(hue2, 0.35f, 0.14f)
    val angle = (Math.abs(h) % 180).toFloat()
    val initials = seed.trim().split("\\s+".toRegex())
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .take(2)
    return ArtParams(c1, c2, angle, initials)
}

// ── SrChip ──────────────────────────────────────────────────
@Composable
fun SrChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (selected) SrPrimary else Color.White.copy(alpha = 0.06f)
            )
            .then(
                if (!selected) Modifier.padding(1.dp).clip(RoundedCornerShape(100.dp)) else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = if (selected) SrOnPrimary else SrText,
        )
    }
}

// ── SrSectionHeader ─────────────────────────────────────────
@Composable
fun SrSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    action: String? = null,
    onAction: () -> Unit = {},
    big: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = SrTextMute,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Text(
                text = title,
                style = if (big) MaterialTheme.typography.displayLarge
                        else MaterialTheme.typography.headlineSmall,
                color = SrText,
            )
        }
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = SrTextDim,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(4.dp),
            )
        }
    }
}

// ── SrEyebrow ───────────────────────────────────────────────
@Composable
fun SrEyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = SrTextMute,
        modifier = modifier,
    )
}
