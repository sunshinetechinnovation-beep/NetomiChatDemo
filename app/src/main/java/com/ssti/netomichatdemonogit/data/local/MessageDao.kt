package com.ssti.netomichatdemonogit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssti.netomichatdemonogit.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    // Get all messages for a specific chat
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<Message>>

    // Get only pending (offline queued) messages
    @Query("SELECT * FROM messages WHERE isPending = 1")
    suspend fun getPendingMessages(): List<Message>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    // Mark message as sent after successful WebSocket delivery
    @Query("UPDATE messages SET isPending = 0, isSent = 1 WHERE id = :messageId")
    suspend fun markAsSent(messageId: Long)

    // Clear everything when app closes (requirement)
    @Query("DELETE FROM messages")
    suspend fun clearAllMessages()
}