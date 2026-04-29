package com.serenade.app.feature.upload.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.upload.data.UploadFileInfo
import com.serenade.app.ui.design.ArtworkAvatar
import com.serenade.app.ui.design.SrChip
import com.serenade.app.ui.design.SrEyebrow
import com.serenade.app.ui.design.SrScreenBackground
import com.serenade.app.ui.design.SrSurfaceCard
import com.serenade.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onBack: () -> Unit,
    viewModel: UploadViewModel,
) {
    val state by viewModel.state.collectAsState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let(viewModel::selectFile)
    }
    val artworkPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(viewModel::selectArtwork)
    }

    Scaffold(
        containerColor = SrBg,
        topBar = {
            TopAppBar(
                title = { Text("Studio", style = MaterialTheme.typography.displaySmall, color = SrText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SrText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SrBg),
            )
        },
    ) { padding ->
        SrScreenBackground(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                SrEyebrow(text = "Upload to studio", modifier = Modifier.padding(top = 6.dp))
                FilePickerCard(
                    file = state.selectedFile,
                    onPickFile = { picker.launch(AUDIO_MIME_TYPES) },
                )

                ArtworkPickerCard(
                    artworkUri = state.artworkUri,
                    onPickArtwork = { artworkPicker.launch("image/*") },
                )

                UploadTextField(state.title, viewModel::updateTitle, "Title")
                UploadTextField(state.artist, viewModel::updateArtist, "Artist")
                UploadTextField(state.album, viewModel::updateAlbum, "Album")

                GenrePicker(
                    selected = state.genre,
                    onSelected = viewModel::updateGenre,
                )

                UploadStatus(state = state)

                Button(
                    onClick = viewModel::upload,
                    enabled = state.canUpload,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SrPrimary,
                        contentColor = SrOnPrimary,
                        disabledContainerColor = SrSurfaceHi,
                        disabledContentColor = SrTextMute,
                    ),
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.trackId == null) "Upload" else "Upload again")
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ArtworkPickerCard(
    artworkUri: Uri?,
    onPickArtwork: () -> Unit,
) {
    SrSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPickArtwork),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (artworkUri != null) {
                AsyncImage(
                    model = artworkUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                )
            } else {
                ArtworkAvatar(seed = "Artwork", size = 42.dp, cornerRadius = 8.dp)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (artworkUri != null) "Artwork selected" else "Add artwork (optional)",
                    style = MaterialTheme.typography.titleMedium,
                    color = SrText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "JPEG, PNG, WEBP · max 5 MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = SrTextDim,
                )
            }
        }
    }
}

@Composable
private fun FilePickerCard(
    file: UploadFileInfo?,
    onPickFile: () -> Unit,
) {
    SrSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPickFile),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(SrSurfaceHi),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = SrPrimary)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file?.name ?: "Choose audio file",
                    style = MaterialTheme.typography.titleMedium,
                    color = SrText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = file?.let { formatFileMeta(it) } ?: "MP3, FLAC, OGG, WAV, M4A, AAC",
                    style = MaterialTheme.typography.bodySmall,
                    color = SrTextDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun UploadTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = SrText,
            unfocusedTextColor = SrText,
            focusedLabelColor = SrPrimary,
            unfocusedLabelColor = SrTextMute,
            focusedBorderColor = SrPrimary,
            unfocusedBorderColor = SrLineHi,
            focusedContainerColor = SrSurface.copy(alpha = 0.6f),
            unfocusedContainerColor = SrSurface.copy(alpha = 0.6f),
        ),
    )
}

@Composable
private fun GenrePicker(
    selected: Genre,
    onSelected: (Genre) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SrEyebrow("Genre")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Genre.entries, key = { it.name }) { genre ->
                SrChip(
                    label = genre.name,
                    selected = genre == selected,
                    onClick = { onSelected(genre) },
                )
            }
        }
    }
}

@Composable
private fun UploadStatus(state: UploadUiState) {
    when {
        state.isUploading -> {
            LinearProgressIndicator(
                progress = { state.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = SrPrimary,
                trackColor = SrSurfaceHi,
            )
            Text(
                text = "Uploading ${state.progressPercent}%",
                style = MaterialTheme.typography.bodyMedium,
                color = SrTextDim,
            )
        }
        state.isPolling -> StatusRow(
            icon = { CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) },
            text = "Processing upload",
        )
        state.status == "READY" -> StatusRow(
            icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
            text = "Ready to play",
        )
        state.status == "FAILED" -> StatusRow(
            icon = { Icon(Icons.Default.Error, contentDescription = null) },
            text = "Processing failed",
            isError = true,
        )
        state.error != null -> StatusRow(
            icon = { Icon(Icons.Default.Error, contentDescription = null) },
            text = state.error,
            isError = true,
        )
    }
}

@Composable
private fun StatusRow(
    icon: @Composable () -> Unit,
    text: String,
    isError: Boolean = false,
) {
    val color = if (isError) SrCoral else SrPrimary
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            icon()
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatFileMeta(file: UploadFileInfo): String {
    val size = file.sizeBytes?.let(::formatBytes) ?: "unknown size"
    return "$size • ${file.contentType ?: "audio"}"
}

private fun formatBytes(bytes: Long): String {
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1.0) "%.1f MB".format(mb) else "${bytes / 1024} KB"
}

private val AUDIO_MIME_TYPES = arrayOf(
    "audio/mpeg",
    "audio/flac",
    "audio/ogg",
    "audio/wav",
    "audio/x-wav",
    "audio/mp4",
    "audio/aac",
    "audio/*",
)
