package com.serenade.app.feature.auth.data

import com.serenade.app.feature.auth.data.entity.UserEntity
import com.serenade.app.feature.auth.data.remote.AuthApiService
import com.serenade.app.feature.auth.data.remote.dto.ForgotPasswordRequest
import com.serenade.app.feature.auth.data.remote.dto.LoginRequest
import com.serenade.app.feature.auth.data.remote.dto.RefreshRequest
import com.serenade.app.feature.auth.data.remote.dto.RegisterRequest
import com.serenade.app.feature.auth.data.remote.dto.RegistrationResponse
import com.serenade.app.feature.auth.data.remote.dto.ResendVerificationRequest
import com.serenade.app.feature.auth.data.remote.dto.ResetPasswordRequest
import com.serenade.app.feature.auth.data.remote.dto.VerifyEmailRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val tokenStore: SecureTokenStore,
    private val userDao: UserDao,
) {
    suspend fun login(email: String, password: String) {
        val resp = api.login(LoginRequest(email, password))
        tokenStore.saveTokens(resp.accessToken, resp.refreshToken)
        userDao.insertUser(UserEntity(id = resp.userId, username = resp.username, email = email))
    }

    suspend fun register(username: String, email: String, password: String): RegistrationResponse =
        api.register(RegisterRequest(username, email, password))

    suspend fun verifyEmail(email: String, code: String) {
        val resp = api.verifyEmail(VerifyEmailRequest(email, code))
        tokenStore.saveTokens(resp.accessToken, resp.refreshToken)
        userDao.insertUser(UserEntity(id = resp.userId, username = resp.username, email = email))
    }

    suspend fun resendVerification(email: String): String? =
        api.resendVerification(ResendVerificationRequest(email)).expiresAt

    suspend fun forgotPassword(email: String) {
        api.forgotPassword(ForgotPasswordRequest(email))
    }

    suspend fun resetPassword(email: String, code: String, newPassword: String) {
        api.resetPassword(ResetPasswordRequest(email, code, newPassword))
    }

    suspend fun refresh(): Boolean {
        val refreshToken = tokenStore.getRefreshToken() ?: return false
        return runCatching {
            val resp = api.refresh(RefreshRequest(refreshToken))
            tokenStore.saveTokens(resp.accessToken, resp.refreshToken)
            true
        }.getOrDefault(false)
    }

    fun isLoggedIn(): Boolean = tokenStore.getAccessToken() != null

    fun logout() {
        tokenStore.clear()
    }
}
