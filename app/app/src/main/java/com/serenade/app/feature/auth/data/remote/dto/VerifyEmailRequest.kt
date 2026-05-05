package com.serenade.app.feature.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifyEmailRequest(val email: String, val code: String)
