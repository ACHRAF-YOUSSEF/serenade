package com.serenade.app.feature.sync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.serenade.app.feature.sync.data.entity.PendingOpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOpDao {
    @Query("SELECT * FROM pending_ops ORDER BY createdAt ASC")
    fun getAll(): Flow<List<PendingOpEntity>>

    @Query("SELECT * FROM pending_ops ORDER BY createdAt ASC")
    suspend fun getPendingList(): List<PendingOpEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(op: PendingOpEntity)

    @Query("DELETE FROM pending_ops WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pending_ops")
    suspend fun deleteAll()
}