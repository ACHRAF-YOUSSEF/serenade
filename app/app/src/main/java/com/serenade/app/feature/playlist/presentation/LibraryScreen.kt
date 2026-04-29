package com.serenade.app.feature.playlist.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.serenade.app.feature.playlist.data.remote.dto.PlaylistSummaryResponse
import com.serenade.app.ui.design.ArtworkAvatar
import com.serenade.app.ui.design.SrChip
import com.serenade.app.ui.design.SrScreenBackground
import com.serenade.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onPlaylistClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: LibraryViewModel,
) {
    val state by viewModel.state.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = SrBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Your library", style = MaterialTheme.typography.displaySmall, color = SrText)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SrText)
                    }
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create playlist", tint = SrText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SrBg),
            )
        },
    ) { padding ->
        SrScreenBackground(modifier = Modifier.padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SrChip(label = "All", selected = true)
                    SrChip(label = "Playlists")
                    SrChip(label = "Albums")
                    SrChip(label = "Downloaded")
                }
                when (val s = state) {
                    is LibraryUiState.Loading -> Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = SrPrimary)
                    }

                    is LibraryUiState.Error -> Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(s.message, color = SrCoral)
                    }

                    is LibraryUiState.Ready -> {
                        if (s.playlists.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("No playlists yet", style = MaterialTheme.typography.bodyLarge, color = SrTextDim)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(top = 10.dp, bottom = 20.dp),
                            ) {
                                items(s.playlists, key = { it.id }) { playlist ->
                                    PlaylistRow(
                                        playlist = playlist,
                                        onClick = { onPlaylistClick(playlist.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            confirmButton = {
                TextButton(
                    enabled = playlistName.isNotBlank(),
                    onClick = {
                        val name = playlistName.trim()
                        viewModel.createPlaylist(name) { playlist ->
                            playlistName = ""
                            showCreateDialog = false
                            onPlaylistClick(playlist.id)
                        }
                    },
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("New playlist") },
            text = {
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            containerColor = SrSurface,
            titleContentColor = SrText,
            textContentColor = SrTextDim,
        )
    }
}

@Composable
private fun PlaylistRow(
    playlist: PlaylistSummaryResponse,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ArtworkAvatar(
            seed = playlist.name,
            size = 48.dp,
            cornerRadius = 9.dp,
            modifier = Modifier.clip(RoundedCornerShape(9.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                playlist.name,
                style = MaterialTheme.typography.titleSmall,
                color = SrText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${playlist.trackCount} tracks • rating ${formatRating(playlist.ratingAvg)}",
                style = MaterialTheme.typography.bodySmall,
                color = SrTextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatRating(value: Double): String = if (value <= 0.0) "none" else "%.1f".format(value)
