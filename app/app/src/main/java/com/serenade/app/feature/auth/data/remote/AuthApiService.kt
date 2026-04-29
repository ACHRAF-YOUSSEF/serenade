package com.serenade.app.feature.auth.data.remote

import com.serenade.app.feature.auth.data.remote.dto.AuthResponse
import com.serenade.app.feature.auth.data.remote.dto.CodeRefreshResponse
import com.serenade.app.feature.auth.data.remote.dto.ForgotPasswordRequest
import com.serenade.app.feature.auth.data.remote.dto.LoginRequest
import com.serenade.app.feature.auth.data.remote.dto.RefreshRequest
import com.serenade.app.feature.auth.data.remote.dto.RegisterRequest
import com.serenade.app.feature.auth.data.remote.dto.RegistrationResponse
import com.serenade.app.feature.auth.data.remote.dto.ResendVerificationRequest
import com.serenade.app.feature.auth.data.remote.dto.ResetPasswordRequest
import com.serenade.app.feature.auth.data.remote.dto.VerifyEmailRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    suspend fun register(@Body req: RegisterRequest): RegistrationResponse

    @POST("api/auth/verify-email")
    suspend fun verifyEmail(@Body req: VerifyEmailRequest): AuthResponse

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Body req: ResendVerificationRequest): CodeRefreshResponse

    @POST("api/auth/login")
    suspend fun login(@Body req: LoginRequest): AuthResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body req: RefreshRequest): AuthResponse

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body req: ForgotPasswordRequest)

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body req: ResetPasswordRequest)
}
