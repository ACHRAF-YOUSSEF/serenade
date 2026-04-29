package com.serenade.app.feature.sync.data.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val PendingOpJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
data class CreatePlaylistOpPayload(
    val localId: String,
    val name: String,
)

@Serializable
data class CopyPlaylistOpPayload(
    val sourcePlaylistId: String,
)

@Serializable
data class RateOpPayload(
    val targetId: String,
    val value: Int,
)

@Serializable
data class AddPlaylistTrackOpPayload(
    val playlistId: String,
    val trackId: String,
)

@Serializable
data class RemovePlaylistTrackOpPayload(
    val playlistId: String,
    val trackId: String,
)

@Serializable
data class ReorderPlaylistTracksOpPayload(
    val playlistId: String,
    val orderedTrackIds: List<String>,
)

@Serializable
data class UploadTrackOpPayload(
    val localFilePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val genre: String,
)
