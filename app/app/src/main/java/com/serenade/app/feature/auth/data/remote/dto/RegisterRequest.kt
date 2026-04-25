package com.serenade.app.feature.auth.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String)
