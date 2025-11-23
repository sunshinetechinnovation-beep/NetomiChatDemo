package com.ssti.netomichatdemonogit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssti.netomichatdemonogit.data.model.Message

@Database(entities = [Message::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}