package com.serenade.app.feature.track.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.serenade.app.core.database.Genre
import com.serenade.app.feature.track.data.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title ASC")
    abstract fun getAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    abstract fun getById(id: String): Flow<TrackEntity?>

    @Query("SELECT * FROM tracks WHERE id = :id")
    abstract suspend fun getByIdOnce(id: String): TrackEntity?

    @Query("SELECT * FROM tracks WHERE genre = :genre ORDER BY title ASC")
    abstract fun getByGenre(genre: Genre): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isDownloaded = 1")
    abstract fun getDownloaded(): Flow<List<TrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(tracks: List<TrackEntity>)

    @Update
    abstract suspend fun update(track: TrackEntity)

    @Query("UPDATE tracks SET localPath = :localPath, isDownloaded = :isDownloaded WHERE id = :id")
    abstract suspend fun updateDownloadState(id: String, localPath: String?, isDownloaded: Boolean)

    @Query("DELETE FROM tracks WHERE id = :id")
    abstract suspend fun deleteById(id: String)

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    abstract suspend fun getByIds(ids: List<String>): List<TrackEntity>

    @Transaction
    open suspend fun upsertFromRemote(track: TrackEntity) {
        val existing = getByIdOnce(track.id)
        insert(
            track.copy(
                localPath = existing?.localPath,
                isDownloaded = existing?.isDownloaded ?: false,
            )
        )
    }

    @Transaction
    open suspend fun upsertAllFromRemote(tracks: List<TrackEntity>) {
        tracks.forEach { upsertFromRemote(it) }
    }
}
