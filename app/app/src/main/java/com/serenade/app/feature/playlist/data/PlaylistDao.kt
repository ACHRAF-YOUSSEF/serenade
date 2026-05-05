package com.serenade.app.feature.playlist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.serenade.app.feature.playlist.data.entity.PlaylistEntity
import com.serenade.app.feature.playlist.data.entity.PlaylistTrackCrossRef
import com.serenade.app.feature.track.data.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithTrackCount(
    @Embedded val playlist: PlaylistEntity,
    val trackCount: Int,
)

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name ASC")
    fun getAll(): Flow<List<PlaylistEntity>>

    @Query(
        """
        SELECT playlists.*, COUNT(refs.trackId) AS trackCount
        FROM playlists
        LEFT JOIN playlist_track_cross_refs refs ON refs.playlistId = playlists.id
        GROUP BY playlists.id
        ORDER BY playlists.name ASC
        """
    )
    fun getAllWithTrackCounts(): Flow<List<PlaylistWithTrackCount>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    fun getById(id: String): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getByIdOnce(id: String): PlaylistEntity?

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

    @Query(
        """
        SELECT tracks.*
        FROM tracks
        INNER JOIN playlist_track_cross_refs refs ON refs.trackId = tracks.id
        WHERE refs.playlistId = :playlistId
        ORDER BY refs.position ASC
        """
    )
    fun getTracksForPlaylist(playlistId: String): Flow<List<TrackEntity>>

    @Query(
        """
        SELECT tracks.*
        FROM tracks
        INNER JOIN playlist_track_cross_refs refs ON refs.trackId = tracks.id
        WHERE refs.playlistId = :playlistId
        ORDER BY refs.position ASC
        """
    )
    suspend fun getTracksForPlaylistOnce(playlistId: String): List<TrackEntity>

    @Query("SELECT * FROM playlist_track_cross_refs WHERE playlistId = :playlistId ORDER BY position ASC")
    fun getTrackRefsForPlaylist(playlistId: String): Flow<List<PlaylistTrackCrossRef>>

    @Query("DELETE FROM playlist_track_cross_refs WHERE playlistId = :playlistId")
    suspend fun clearTracksForPlaylist(playlistId: String)

    @Query("DELETE FROM playlists")
    suspend fun deleteAll()

    @Query("DELETE FROM playlist_track_cross_refs")
    suspend fun deleteAllCrossRefs()
}
