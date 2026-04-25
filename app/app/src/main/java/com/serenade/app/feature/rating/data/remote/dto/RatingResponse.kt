package com.serenade.app.feature.rating.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingResponse(
    val id: String,
    val targetType: String,
    val targetId: String,
    val value: Int,
    val avg: Double,
)
