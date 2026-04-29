package com.serenade.app.feature.splash.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.serenade.app.ui.theme.SrBg
import com.serenade.app.ui.theme.SrBgDeep
import com.serenade.app.ui.theme.SrCoral
import com.serenade.app.ui.theme.SrPlum
import com.serenade.app.ui.theme.SrPrimary
import com.serenade.app.ui.theme.SrPrimaryHi
import com.serenade.app.ui.theme.SrText
import com.serenade.app.ui.theme.SrTextMute
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1_150)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(SrPlum.copy(alpha = 0.34f), SrBg, SrBgDeep),
                    center = Offset(0.5f, 0.3f),
                    radius = 900f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        listOf(280.dp, 220.dp, 160.dp, 100.dp).forEachIndexed { index, size ->
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.04f + index * 0.025f),
                        shape = CircleShape,
                    ),
            )
        }
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(SrPrimaryHi, SrCoral)))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
        ) {
            Text(
                text = "S",
                style = MaterialTheme.typography.displaySmall.copy(fontStyle = FontStyle.Italic),
                color = SrBgDeep,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Text(
                text = "Serenade",
                style = MaterialTheme.typography.displayMedium,
                color = SrText,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "MUSIC FOR AFTER DARK",
                style = MaterialTheme.typography.labelSmall,
                color = SrTextMute,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(58.dp))
        }
    }
}
