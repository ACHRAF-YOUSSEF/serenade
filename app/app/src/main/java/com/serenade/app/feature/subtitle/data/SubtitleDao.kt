package com.serenade.app.feature.subtitle.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.serenade.app.feature.subtitle.data.entity.SubtitleLineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtitleDao {
    @Query("SELECT * FROM subtitle_lines WHERE trackId = :trackId ORDER BY startMs ASC")
    fun getByTrackId(trackId: String): Flow<List<SubtitleLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lines: List<SubtitleLineEntity>)

    @Query("DELETE FROM subtitle_lines WHERE trackId = :trackId")
    suspend fun deleteByTrackId(trackId: String)
}