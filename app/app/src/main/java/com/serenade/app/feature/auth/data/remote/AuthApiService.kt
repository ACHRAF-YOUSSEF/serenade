package com.serenade.app.feature.auth.data.remote

import com.serenade.app.feature.auth.data.remote.dto.AuthResponse
import com.serenade.app.feature.auth.data.remote.dto.LoginRequest
import com.serenade.app.feature.auth.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse
}
