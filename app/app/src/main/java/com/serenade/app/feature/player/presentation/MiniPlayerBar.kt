package com.serenade.app.feature.player.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.serenade.app.feature.player.PlaybackState
import com.serenade.app.ui.design.ArtworkAvatar
import com.serenade.app.ui.theme.*

@Composable
fun MiniPlayerBar(
    state: PlaybackState,
    trackTitle: String?,
    trackArtist: String?,
    artworkUrl: String?,
    onTogglePlayPause: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = state.currentTrackId != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1F1530).copy(alpha = 0.92f))
                    .clickable(onClick = onBarClick)
                    .padding(8.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Artwork
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        ArtworkAvatar(
                            seed = trackTitle ?: "track",
                            size = 44.dp,
                            cornerRadius = 8.dp,
                        )
                        if (!artworkUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = artworkUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }

                    // Track info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = trackTitle ?: "Unknown",
                            style = MaterialTheme.typography.titleSmall,
                            color = SrText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = trackArtist ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = SrTextDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    // Play/pause button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SrPrimary)
                            .clickable(onClick = onTogglePlayPause),
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) "Pause" else "Play",
                            tint = SrOnPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                // Progress hairline at bottom
                if (state.durationMs > 0) {
                    val progress = (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 0.dp)
                            .fillMaxWidth()
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .background(SrPrimary),
                        )
                    }
                }
            }
        }
    }
}
