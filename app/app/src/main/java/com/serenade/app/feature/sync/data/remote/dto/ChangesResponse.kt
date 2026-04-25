package com.serenade.app.feature.sync.data.remote.dto

import com.serenade.app.feature.playlist.data.remote.dto.PlaylistSummaryResponse
import com.serenade.app.feature.rating.data.remote.dto.RatingResponse
import com.serenade.app.feature.track.data.remote.dto.TrackResponse
import kotlinx.serialization.Serializable

@Serializable
data class ChangesResponse(
    val tracks: List<TrackResponse> = emptyList(),
    val playlists: List<PlaylistSummaryResponse> = emptyList(),
    val ratings: List<RatingResponse> = emptyList(),
    val nextCursor: String,
)
