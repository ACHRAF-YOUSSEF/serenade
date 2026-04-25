package com.serenade.app.feature.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: String,
    val username: String,
)
