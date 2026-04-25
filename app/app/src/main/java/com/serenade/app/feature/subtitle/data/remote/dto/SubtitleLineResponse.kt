package com.serenade.app.feature.subtitle.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubtitleLineResponse(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
)
