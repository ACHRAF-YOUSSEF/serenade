package com.serenade.app.core.database

import androidx.room.TypeConverter
import com.serenade.app.feature.download.data.entity.DownloadState
import com.serenade.app.feature.rating.data.entity.RatingTargetType
import java.time.Instant

class AppConverters {
    @TypeConverter
    fun fromInstant(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun toInstant(millis: Long?): Instant? = millis?.let {
        Instant.ofEpochMilli(it)
    }

    @TypeConverter
    fun fromGenre(genre: Genre?): String? = genre?.name

    @TypeConverter
    fun toGenre(name: String?): Genre? = name?.let {
        enumValueOf<Genre>(it)
    }

    @TypeConverter
    fun fromDownloadState(state: DownloadState?): String? = state?.name

    @TypeConverter
    fun toDownloadState(name: String?): DownloadState? = name?.let {
        enumValueOf<DownloadState>(it)
    }

    @TypeConverter
    fun fromRatingTargetType(type: RatingTargetType?): String? = type?.name

    @TypeConverter
    fun toRatingTargetType(name: String?): RatingTargetType? = name?.let {
        enumValueOf<RatingTargetType>(it)
    }
}