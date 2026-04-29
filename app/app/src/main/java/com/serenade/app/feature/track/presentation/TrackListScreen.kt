package com.serenade.app.feature.track.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.serenade.app.feature.download.data.entity.DownloadEntity
import com.serenade.app.feature.download.data.entity.DownloadState
import com.serenade.app.feature.track.data.entity.TrackEntity

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Serenade") },
                actions = {
                    IconButton(onClick = onLibraryClick) {
                        Icon(Icons.Default.LibraryMusic, contentDescription = "Library")
                    }
                    IconButton(onClick = onDownloadsClick) {
                        Icon(Icons.Default.DownloadForOffline, contentDescription = "Downloads")
                    }
                    IconButton(onClick = onUploadClick) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload")
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = syncing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (val s = state) {
                is TrackListUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is TrackListUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.message, color = MaterialTheme.colorScheme.error)
                }
                is TrackListUiState.Ready -> {
                    if (s.tracks.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No tracks yet", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(s.tracks, key = { it.id }) { track ->
                                TrackRow(
                                    track = track,
                                    download = downloadsByTrackId[track.id],
                                    onClick = { onTrackClick(track, s.tracks) },
                                    onDownloadClick = { viewModel.queueDownload(track) },
                                    onDeleteDownload = { viewModel.deleteDownload(track.id) },
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: TrackEntity,
    download: DownloadEntity?,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onDeleteDownload: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, contentDescription = null)
                    if (!track.artworkUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = track.artworkUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                track.durationMs.takeIf { it > 0 }?.let {
                    Text(formatDuration(it), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.width(8.dp))
                DownloadAction(
                    download = download,
                    hasStream = !track.streamUrl.isNullOrBlank(),
                    onDownloadClick = onDownloadClick,
                    onDeleteDownload = onDeleteDownload,
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun DownloadAction(
    download: DownloadEntity?,
    hasStream: Boolean,
    onDownloadClick: () -> Unit,
    onDeleteDownload: () -> Unit,
) {
    when (download?.state) {
        DownloadState.DONE -> {
            IconButton(onClick = onDeleteDownload) {
                Icon(Icons.Default.Delete, contentDescription = "Delete download")
            }
        }
        DownloadState.DOWNLOADING, DownloadState.QUEUED -> {
            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { (download.progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        else -> {
            IconButton(onClick = onDownloadClick, enabled = hasStream) {
                Icon(
                    imageVector = if (download?.state == DownloadState.FAILED) Icons.Default.DownloadDone else Icons.Default.Download,
                    contentDescription = if (download?.state == DownloadState.FAILED) "Retry download" else "Download",
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
