package com.serenade.app.feature.playlist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.serenade.app.feature.track.data.entity.TrackEntity
import com.serenade.app.feature.track.data.remote.dto.TrackResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    onTrackClick: (TrackResponse) -> Unit,
    onBack: () -> Unit,
    viewModel: PlaylistDetailViewModel,
) {
    val state by viewModel.state.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            val title = (state as? PlaylistDetailUiState.Ready)?.detail?.name ?: "Playlist"
            TopAppBar(
                title = {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.copyPlaylist(onBack) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy playlist")
                    }
                },
            )
        },
        floatingActionButton = {
            if (state is PlaylistDetailUiState.Ready) {
                FloatingActionButton(onClick = { showAddSheet = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add tracks")
                }
            }
        },
    ) { padding ->
        when (val s = state) {
            is PlaylistDetailUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is PlaylistDetailUiState.Error -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }

            is PlaylistDetailUiState.Ready -> {
                val currentTrackIds = remember(s.detail.tracks) {
                    s.detail.tracks.map { it.id }.toHashSet()
                }

                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    PlaylistSummaryHeader(
                        trackCount = s.detail.tracks.size,
                        averageRating = s.detail.ratingAvg,
                        selectedRating = s.myRating,
                        onRate = viewModel::rate,
                    )
                    HorizontalDivider()
                    if (s.detail.tracks.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("No tracks in playlist", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(s.detail.tracks, key = { it.id }) { track ->
                                PlaylistTrackRow(
                                    track = track,
                                    onClick = { onTrackClick(track) },
                                    onRemove = { viewModel.removeTrack(track.id) },
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }

                if (showAddSheet) {
                    val availableTracks = remember(allTracks, currentTrackIds) {
                        allTracks.filter { it.id !in currentTrackIds }
                    }
                    ModalBottomSheet(onDismissRequest = { showAddSheet = false }) {
                        Text(
                            "Add tracks",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        if (availableTracks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No more tracks available")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding(),
                            ) {
                                items(availableTracks, key = { it.id }) { track ->
                                    AddTrackRow(
                                        track = track,
                                        onClick = {
                                            viewModel.addTrack(track.id)
                                            showAddSheet = false
                                        },
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
}

@Composable
private fun PlaylistSummaryHeader(
    trackCount: Int,
    averageRating: Double,
    selectedRating: Int?,
    onRate: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "$trackCount tracks • average ${formatRating(averageRating)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            (1..5).forEach { rating ->
                IconButton(onClick = { onRate(rating) }) {
                    Icon(
                        imageVector = if ((selectedRating ?: 0) >= rating) {
                            Icons.Default.Star
                        } else {
                            Icons.Default.StarBorder
                        },
                        contentDescription = "Rate $rating stars",
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistTrackRow(track: TrackResponse, onClick: () -> Unit, onRemove: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = {
            Icon(Icons.Default.MusicNote, contentDescription = null)
        },
        trailingContent = {
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove track")
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun AddTrackRow(track: TrackEntity, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(track.artist, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = {
            Icon(Icons.Default.MusicNote, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

private fun formatRating(value: Double): String = if (value <= 0.0) "none" else "%.1f".format(value)
