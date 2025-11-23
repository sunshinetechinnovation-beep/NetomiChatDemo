package com.ssti.netomichatdemonogit.di

import android.content.Context
import androidx.room.Room
import com.ssti.netomichatdemonogit.data.local.AppDatabase
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "netomi_chat_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(db: AppDatabase) = db.messageDao()
}