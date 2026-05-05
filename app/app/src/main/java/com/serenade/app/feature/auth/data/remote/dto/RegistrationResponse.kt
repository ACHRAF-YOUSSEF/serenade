package com.serenade.app.feature.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegistrationResponse(
    val userId: String,
    val email: String,
    val verificationRequired: Boolean,
    val expiresAt: String? = null,
)
