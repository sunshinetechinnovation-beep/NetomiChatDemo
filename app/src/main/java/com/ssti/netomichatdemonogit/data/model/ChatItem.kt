package com.ssti.netomichatdemonogit.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents one chatbot conversation (e.g., Support Bot, Sales Bot)
data class ChatItem(
    val chatId: String,              // Unique ID for each bot (e.g., "support", "sales")
    val botName: String,
    val avatarUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageTime: Long = 0L,
    val unreadCount: Int = 0
)

// Single message inside a conversation
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val chatId: String,              // Which bot this belongs to
    val text: String,
    val isFromUser: Boolean,         // true = user sent, false = bot reply
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean = true,      // false = queued when offline
    val isPending: Boolean = false   // true = waiting to be sent
)