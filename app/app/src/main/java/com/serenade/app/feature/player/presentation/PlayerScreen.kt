package com.serenade.app.feature.player.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.serenade.app.ui.design.ArtworkAvatar
import com.serenade.app.ui.design.SrScreenBackground
import com.serenade.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    trackTitle: String,
    trackArtist: String,
    trackDurationMs: Long,
    artworkUrl: String?,
    onDismiss: () -> Unit,
    viewModel: PlayerViewModel
) {
    val state by viewModel.state.collectAsState()
    val currentCue by viewModel.currentCue.collectAsState()

    Scaffold(
        containerColor = SrBg,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse", tint = SrText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SrBg),
            )
        }
    ) { padding ->
        SrScreenBackground(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(SrSurfaceHi),
                ) {
                    ArtworkAvatar(
                        seed = trackTitle.ifBlank { "Now Playing" },
                        size = 280.dp,
                        cornerRadius = 18.dp,
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

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = trackTitle,
                    style = MaterialTheme.typography.displaySmall,
                    color = SrText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trackArtist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SrTextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                if (state.queueSize > 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${state.queueIndex + 1} / ${state.queueSize}",
                        style = MaterialTheme.typography.labelMedium,
                        color = SrTextMute,
                        textAlign = TextAlign.Center,
                    )
                }

            Spacer(modifier = Modifier.height(32.dp))

            val effectiveDurationMs = if (state.durationMs > 0) state.durationMs else trackDurationMs
            val durationKnown = effectiveDurationMs > 0
            data class DragState(var progress: Float = 0f, var durationMs: Long = 0L)
            val drag = remember { DragState() }
            var isDragging by remember { mutableStateOf(false) }
            var dragDisplayProgress by remember { mutableFloatStateOf(0f) }
            val displayProgress = if (isDragging) dragDisplayProgress
                else if (durationKnown) state.positionMs.toFloat() / effectiveDurationMs else 0f
            Slider(
                value = displayProgress,
                onValueChange = {
                    if (!isDragging) drag.durationMs = effectiveDurationMs
                    drag.progress = it
                    dragDisplayProgress = it
                    isDragging = true
                },
                onValueChangeFinished = {
                    if (drag.durationMs > 0L) {
                        viewModel.seekTo((drag.progress * drag.durationMs).toLong())
                    }
                    isDragging = false
                },
                enabled = durationKnown,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = SrPrimary,
                    activeTrackColor = SrPrimary,
                    inactiveTrackColor = SrSurfaceHi,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatMs(state.positionMs), style = MaterialTheme.typography.labelSmall, color = SrTextMute)
                Text(
                    text = if (durationKnown) formatMs(effectiveDurationMs) else "--:--",
                    style = MaterialTheme.typography.labelSmall,
                    color = SrTextMute,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = currentCue != null) {
                Text(
                    text = currentCue ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SrText,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                modifier = Modifier.fillMaxWidth(),
            ) {
                IconButton(
                    onClick = viewModel::skipToPrevious,
                    enabled = state.hasPrevious,
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                }
                FilledIconButton(
                    onClick = viewModel::togglePlayPause,
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = SrPrimary,
                        contentColor = SrOnPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(36.dp),
                    )
                }
                IconButton(
                    onClick = viewModel::skipToNext,
                    enabled = state.hasNext,
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
                }
            }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
