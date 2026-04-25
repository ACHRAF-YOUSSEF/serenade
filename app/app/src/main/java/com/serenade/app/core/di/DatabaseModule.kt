package com.serenade.app.core.di

import android.content.Context
import androidx.room.Room
import com.serenade.app.BuildConfig
import com.serenade.app.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        val builder = Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = AppDatabase.DATABASE_NAME
        )

        if (BuildConfig.DEBUG) {
            builder.fallbackToDestructiveMigration(dropAllTables = true)
        }

        return builder.build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    @Singleton
    fun provideTrackDao(db: AppDatabase) = db.trackDao()

    @Provides
    @Singleton
    fun providePlaylistDao(db: AppDatabase) = db.playlistDao()

    @Provides
    @Singleton
    fun provideSubtitleDao(db: AppDatabase) = db.subtitleDao()

    @Provides
    @Singleton
    fun provideDownloadDao(db: AppDatabase) = db.downloadDao()

    @Provides
    @Singleton
    fun providePendingOpDao(db: AppDatabase) = db.pendingOpDao()

    @Provides
    @Singleton
    fun provideProviderDao(db: AppDatabase) = db.providerDao()

    @Provides
    @Singleton
    fun provideRatingDao(db: AppDatabase) = db.ratingDao()
}
