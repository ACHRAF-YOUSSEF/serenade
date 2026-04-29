package com.serenade.app.feature.playlist.data.remote

import com.serenade.app.feature.playlist.data.remote.dto.*
import com.serenade.app.feature.track.data.remote.dto.PageResponse
import retrofit2.http.*

interface PlaylistApiService {
    @GET("api/playlists")
    suspend fun getMyPlaylists(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): PageResponse<PlaylistSummaryResponse>

    @GET("api/playlists/{id}")
    suspend fun getPlaylist(@Path("id") id: String): PlaylistDetailResponse

    @POST("api/playlists")
    suspend fun createPlaylist(@Body req: CreatePlaylistRequest): PlaylistSummaryResponse

    @PATCH("api/playlists/{id}")
    suspend fun updatePlaylist(@Path("id") id: String, @Body req: UpdatePlaylistRequest): PlaylistSummaryResponse

    @PUT("api/playlists/{id}/tracks")
    suspend fun setTracks(@Path("id") id: String, @Body tracks: List<TrackPositionRequest>)

    @POST("api/playlists/{id}/copy")
    suspend fun copyPlaylist(@Path("id") id: String): PlaylistSummaryResponse

    @DELETE("api/playlists/{id}")
    suspend fun deletePlaylist(@Path("id") id: String)
}
