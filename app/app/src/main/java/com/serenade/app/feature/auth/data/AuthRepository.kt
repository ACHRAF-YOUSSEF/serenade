package com.serenade.app.feature.auth.data

import com.serenade.app.feature.auth.data.entity.UserEntity
import com.serenade.app.feature.auth.data.remote.AuthApiService
import com.serenade.app.feature.auth.data.remote.dto.LoginRequest
import com.serenade.app.feature.auth.data.remote.dto.RefreshRequest
import com.serenade.app.feature.auth.data.remote.dto.RegisterRequest
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

    suspend fun register(username: String, email: String, password: String) {
        val resp = api.register(RegisterRequest(username, email, password))
        tokenStore.saveTokens(resp.accessToken, resp.refreshToken)
        userDao.insertUser(UserEntity(id = resp.userId, username = resp.username, email = email))
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
