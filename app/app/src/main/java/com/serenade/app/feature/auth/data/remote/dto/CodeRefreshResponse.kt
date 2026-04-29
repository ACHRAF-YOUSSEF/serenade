package com.serenade.app.feature.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CodeRefreshResponse(
    val email: String,
    val expiresAt: String? = null,
)
