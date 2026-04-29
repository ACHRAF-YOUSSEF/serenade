package com.serenade.app.feature.playlist.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePlaylistRequest(val name: String, val version: Int)
