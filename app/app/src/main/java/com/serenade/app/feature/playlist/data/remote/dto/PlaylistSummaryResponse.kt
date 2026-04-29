package com.serenade.app.feature.playlist.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSummaryResponse(
    val id: String,
    val name: String,
    val isCopy: Boolean,
    val sourcePlaylistId: String? = null,
    val version: Int,
    val trackCount: Int,
    val ratingAvg: Double,
    val updatedAt: String? = null,
)
