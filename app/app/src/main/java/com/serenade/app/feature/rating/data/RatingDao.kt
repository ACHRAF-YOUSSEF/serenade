package com.serenade.app.feature.rating.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.serenade.app.feature.rating.data.entity.RatingEntity
import com.serenade.app.feature.rating.data.entity.RatingTargetType
import kotlinx.coroutines.flow.Flow

@Dao
interface RatingDao {
    @Query("SELECT * FROM ratings WHERE targetType = :targetType AND targetId = :targetId")
    fun getByTarget(targetType: RatingTargetType, targetId: String): Flow<List<RatingEntity>>

    @Query("SELECT * FROM ratings WHERE id = :id")
    fun getById(id: String): Flow<RatingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rating: RatingEntity)

    @Update
    suspend fun update(rating: RatingEntity)

    @Query("SELECT * FROM ratings WHERE targetType = :targetType AND targetId = :targetId LIMIT 1")
    suspend fun getByTargetOnce(targetType: RatingTargetType, targetId: String): RatingEntity?

    @Query("DELETE FROM ratings WHERE id = :id")
    suspend fun deleteById(id: String)
}