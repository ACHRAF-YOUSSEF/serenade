package com.serenade.app.feature.rating.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RatingRequest(val targetType: String, val targetId: String, val value: Int)
