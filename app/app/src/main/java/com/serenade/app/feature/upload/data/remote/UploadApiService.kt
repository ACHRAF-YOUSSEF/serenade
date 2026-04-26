package com.serenade.app.feature.upload.data.remote

import kotlinx.serialization.Serializable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface UploadApiService {
    @Multipart
    @POST("api/tracks/upload")
    suspend fun uploadTrack(
        @Part("title") title: RequestBody,
        @Part("artist") artist: RequestBody,
        @Part("album") album: RequestBody?,
        @Part("genre") genre: RequestBody,
        @Part file: MultipartBody.Part,
        @Part artwork: MultipartBody.Part?,
    ): UploadResponse

    @GET("api/uploads/{trackId}")
    suspend fun getUploadStatus(@Path("trackId") trackId: String): UploadStatusResponse
}

@Serializable
data class UploadResponse(
    val trackId: String,
    val status: String,
)

@Serializable
data class UploadStatusResponse(
    val trackId: String,
    val title: String,
    val artist: String,
    val album: String? = null,
    val genre: String,
    val status: String,
    val durationMs: Long? = null,
    val streamUrl: String? = null,
    val updatedAt: String? = null,
)
