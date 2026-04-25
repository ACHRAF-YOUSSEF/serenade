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
