package com.serenade.app.feature.download.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.serenade.app.feature.track.data.entity.TrackEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    onTrackClick: (TrackEntity) -> Unit,
    onBack: () -> Unit,
    viewModel: DownloadViewModel,
) {
    val tracks by viewModel.downloadedTracks.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloads") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        if (tracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("No downloads yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(tracks, key = { it.id }) { track ->
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
                            IconButton(onClick = { viewModel.deleteDownload(track.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete download")
                            }
                        },
                        modifier = Modifier.clickable { onTrackClick(track) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
