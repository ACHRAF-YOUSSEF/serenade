package com.serenade.app.feature.track.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.serenade.app.feature.download.data.entity.DownloadEntity
import com.serenade.app.feature.download.data.entity.DownloadState
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.ui.design.ArtworkAvatar
import com.serenade.app.ui.design.SrEyebrow
import com.serenade.app.ui.design.SrSectionHeader
import com.serenade.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen(
    onTrackClick: (TrackEntity, List<TrackEntity>) -> Unit,
    onSearchClick: () -> Unit,
    onLibraryClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrackListViewModel,
) {
    val state by viewModel.state.collectAsState()
    val syncing by viewModel.syncing.collectAsState()
    val downloadsByTrackId by viewModel.downloadsByTrackId.collectAsState()
    val displayName by viewModel.displayName.collectAsState()

    Box(
        modifier = modifier.background(SrBg),
    ) {
        PullToRefreshBox(
            isRefreshing = syncing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // ── Atmospheric header ──
                item {
                    HomeHeader(displayName = displayName, onSearchClick = onSearchClick)
                }

                when (val s = state) {
                    is TrackListUiState.Loading -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = SrPrimary)
                        }
                    }

                    is TrackListUiState.Error -> item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(s.message, color = SrCoral, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    is TrackListUiState.Ready -> {
                        if (s.tracks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(48.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        "No tracks yet",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = SrTextDim,
                                    )
                                }
                            }
                        } else {
                            item {
                SrSectionHeader(
                    title = "All tracks",
                    eyebrow = "Your library",
                    modifier = Modifier.padding(top = 8.dp),
                )
                            }
                            items(s.tracks, key = { it.id }) { track ->
                                SrTrackRow(
                                    track = track,
                                    download = downloadsByTrackId[track.id],
                                    onClick = { onTrackClick(track, s.tracks) },
                                    onDownloadClick = { viewModel.queueDownload(track) },
                                    onDeleteDownload = { viewModel.deleteDownload(track.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    displayName: String?,
    onSearchClick: () -> Unit,
) {
    val avatarText = displayName
        ?.trim()
        ?.firstOrNull()
        ?.uppercaseChar()
        ?.toString()
        ?: "U"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(SrPlum.copy(alpha = 0.55f), Color.Transparent),
                    radius = 600f,
                )
            ),
    ) {
        // subtle coral accent top-right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(SrCoral.copy(alpha = 0.2f), Color.Transparent),
                        radius = 500f,
                        center = androidx.compose.ui.geometry.Offset(900f, 0f),
                    )
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    SrEyebrow(text = "Your feed")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Good evening.",
                        style = MaterialTheme.typography.headlineLarge,
                        color = SrText,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = SrText)
                    }
                    // Avatar placeholder
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(SrCoral, SrPlum))
                            ),
                    ) {
                        Text(
                            text = avatarText,
                            style = MaterialTheme.typography.titleSmall,
                            color = SrText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SrTrackRow(
    track: TrackEntity,
    download: DownloadEntity?,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteDownload: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Artwork
        Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(6.dp))) {
            ArtworkAvatar(seed = track.title, size = 42.dp, cornerRadius = 6.dp)
            if (!track.artworkUrl.isNullOrBlank()) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Title + artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleSmall,
                color = SrText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = SrTextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Duration
        track.durationMs.takeIf { it > 0 }?.let {
            Text(
                text = formatDuration(it),
                style = MaterialTheme.typography.labelMedium,
                color = SrTextMute,
            )
        }

        // Download indicator
        SrDownloadAction(
            download = download,
            hasStream = !track.streamUrl.isNullOrBlank(),
            onDownloadClick = onDownloadClick,
            onDeleteDownload = onDeleteDownload,
        )

        Icon(Icons.Outlined.MoreVert, contentDescription = null, tint = SrTextMute, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun SrDownloadAction(
    download: DownloadEntity?,
    hasStream: Boolean,
    onDownloadClick: () -> Unit,
    onDeleteDownload: () -> Unit,
) {
    when (download?.state) {
        DownloadState.DONE -> {
            Icon(
                Icons.Default.DownloadDone,
                contentDescription = "Downloaded",
                tint = SrPrimary,
                modifier = Modifier.size(16.dp).clickable(onClick = onDeleteDownload),
            )
        }
        DownloadState.DOWNLOADING, DownloadState.QUEUED -> {
            CircularProgressIndicator(
                progress = { (download.progress / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.size(16.dp),
                color = SrPrimary,
                strokeWidth = 2.dp,
            )
        }
        else -> {
            if (hasStream) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Download",
                    tint = SrTextMute,
                    modifier = Modifier.size(16.dp).clickable(onClick = onDownloadClick),
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}
