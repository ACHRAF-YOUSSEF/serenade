package com.serenade.app.feature.track.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PageResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
)
