package com.serenade.app.feature.playlist.data.remote.dto

import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDetailResponse(
    val id: String,
    val name: String,
    val isCopy: Boolean,
    val sourcePlaylistId: String? = null,
    val version: Int,
    val ratingAvg: Double,
    val tracks: List<TrackResponse>,
)
