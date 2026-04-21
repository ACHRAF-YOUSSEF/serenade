package com.serenade.app.feature.playlist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.serenade.app.feature.playlist.data.entity.PlaylistEntity
import com.serenade.app.feature.playlist.data.entity.PlaylistTrackCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getById(id: String): Flow<PlaylistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity)

    @Update
    suspend fun update(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: PlaylistTrackCrossRef)

    @Delete
    suspend fun deleteCrossRef(crossRef: PlaylistTrackCrossRef)

    @Query("SELECT * FROM playlist_track_cross_refs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getTracksForPlaylist(playlistId: String): Flow<List<PlaylistTrackCrossRef>>

    @Query("DELETE FROM playlist_track_cross_refs WHERE playlistId = :playlistId")
    suspend fun clearTracksForPlaylist(playlistId: String)
}