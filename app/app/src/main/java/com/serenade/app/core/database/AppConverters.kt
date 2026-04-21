package com.serenade.app.core.database

import androidx.room.TypeConverter
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
}