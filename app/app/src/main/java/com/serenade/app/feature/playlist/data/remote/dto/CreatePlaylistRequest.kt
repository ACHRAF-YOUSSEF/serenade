package com.serenade.app.feature.playlist.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreatePlaylistRequest(val name: String)
