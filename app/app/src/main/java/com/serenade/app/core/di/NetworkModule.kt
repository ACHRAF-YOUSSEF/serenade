package com.serenade.app.core.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.serenade.app.BuildConfig
import com.serenade.app.feature.auth.data.SecureTokenStore
import com.serenade.app.feature.auth.data.TokenRefreshAuthenticator
import com.serenade.app.feature.auth.data.remote.AuthApiService
import com.serenade.app.feature.playlist.data.remote.PlaylistApiService
import com.serenade.app.feature.rating.data.remote.RatingApiService
import com.serenade.app.feature.search.presentation.SearchApiService
import com.serenade.app.feature.subtitle.data.remote.SubtitleApiService
import com.serenade.app.feature.sync.data.remote.ChangesApiService
import com.serenade.app.feature.track.data.remote.TrackApiService
import com.serenade.app.feature.upload.data.remote.UploadApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    @AuthenticatedOkHttpClient
    fun provideOkHttpClient(tokenStore: SecureTokenStore, authenticator: TokenRefreshAuthenticator): OkHttpClient {
        return OkHttpClient.Builder()
            .authenticator(authenticator)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = tokenStore.getAccessToken()
                val req = if (token != null) {
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                } else chain.request()
                chain.proceed(req)
            }
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply {
                        redactHeader("Authorization")
                        level = HttpLoggingInterceptor.Level.BODY
                    })
                }
            }
            .build()
    }

    @Provides
    @Singleton
    @PublicOkHttpClient
    fun providePublicOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(300, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(@AuthenticatedOkHttpClient client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideTrackApiService(retrofit: Retrofit): TrackApiService =
        retrofit.create(TrackApiService::class.java)

    @Provides
    @Singleton
    fun provideSubtitleApiService(retrofit: Retrofit): SubtitleApiService =
        retrofit.create(SubtitleApiService::class.java)

    @Provides
    @Singleton
    fun provideSearchApiService(retrofit: Retrofit): SearchApiService =
        retrofit.create(SearchApiService::class.java)

    @Provides
    @Singleton
    fun providePlaylistApiService(retrofit: Retrofit): PlaylistApiService =
        retrofit.create(PlaylistApiService::class.java)

    @Provides
    @Singleton
    fun provideRatingApiService(retrofit: Retrofit): RatingApiService =
        retrofit.create(RatingApiService::class.java)

    @Provides
    @Singleton
    fun provideChangesApiService(retrofit: Retrofit): ChangesApiService =
        retrofit.create(ChangesApiService::class.java)

    @Provides
    @Singleton
    fun provideUploadApiService(retrofit: Retrofit): UploadApiService =
        retrofit.create(UploadApiService::class.java)
}
