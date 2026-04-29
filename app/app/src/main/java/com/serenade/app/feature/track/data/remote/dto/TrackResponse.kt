package com.serenade.app.feature.track.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackResponse(
    val id: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val genre: String,
    val durationMs: Long? = null,
    val artworkUrl: String? = null,
    val streamUrl: String? = null,
    val updatedAt: String? = null,
)
