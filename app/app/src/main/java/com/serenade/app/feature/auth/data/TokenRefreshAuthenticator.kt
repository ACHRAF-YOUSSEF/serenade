package com.serenade.app.feature.auth.data

import com.serenade.app.BuildConfig
import com.serenade.app.feature.auth.data.remote.dto.AuthResponse
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshAuthenticator @Inject constructor(
    private val tokenStore: SecureTokenStore,
) : Authenticator {

    private val json = Json { ignoreUnknownKeys = true }
    private val refreshClient = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        val tokenThatFailed = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
        val currentToken = tokenStore.getAccessToken()

        if (currentToken != null && currentToken != tokenThatFailed) {
            return response.request.newBuilder()
                .header("Authorization", "Bearer $currentToken")
                .build()
        }

        if (response.priorResponse != null) return null

        val refreshToken = tokenStore.getRefreshToken() ?: run {
            tokenStore.clear()
            return null
        }

        val body = """{"refreshToken":"${refreshToken.replace("\"", "\\\"")}"}"""
            .toRequestBody(jsonMediaType)

        val refreshRequest = Request.Builder()
            .url("${BuildConfig.API_BASE_URL}api/auth/refresh")
            .post(body)
            .build()

        return try {
            val refreshResponse = refreshClient.newCall(refreshRequest).execute()
            if (!refreshResponse.isSuccessful) {
                tokenStore.clear()
                return null
            }
            val bodyStr = refreshResponse.body.string()
            val auth = json.decodeFromString<AuthResponse>(bodyStr)
            tokenStore.saveTokens(auth.accessToken, auth.refreshToken)
            response.request.newBuilder()
                .header("Authorization", "Bearer ${auth.accessToken}")
                .build()
        } catch (_: Exception) {
            null
        }
    }
}
