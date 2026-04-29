package com.serenade.app.feature.playlist.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackPositionRequest(val trackId: String, val position: Int)
