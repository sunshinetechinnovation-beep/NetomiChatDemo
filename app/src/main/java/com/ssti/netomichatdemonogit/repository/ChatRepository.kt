package com.ssti.netomichatdemonogit.repository
import com.ssti.netomichatdemonogit.data.local.MessageDao
import com.ssti.netomichatdemonogit.data.model.ChatItem
import com.ssti.netomichatdemonogit.data.model.Message
import com.ssti.netomichatdemonogit.network.PieSocketManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val messageDao: MessageDao
) {
    // Hard-coded bots (as per Netomi style)
    private val botList = listOf(
        ChatItem("support", "Support Bot", unreadCount = 0),
        ChatItem("sales", "Sales Bot", unreadCount = 0),
        ChatItem("billing", "Billing Bot", unreadCount = 0)
    )

    private val _chatList = MutableStateFlow(botList)
    val chatList: StateFlow<List<ChatItem>> = _chatList.asStateFlow()

    // Observe incoming WebSocket messages
    init {
        CoroutineScope(Dispatchers.IO).launch {
            PieSocketManager.incomingMessage.collect { jsonString ->
                jsonString?.let { processIncomingMessage(it) }
            }
        }
    }

    // Send message (with offline queuing)
    suspend fun sendMessage(chatId: String, text: String) {
        if (!PieSocketManager.isOnline) {
            // Queue locally when offline
            val pendingMsg = Message(
                chatId = chatId,
                text = text,
                isFromUser = true,
                isSent = false,
                isPending = true
            )
            messageDao.insert(pendingMsg)
            updateChatPreview(chatId, text, true)
            return
        }

        // Online → send via WebSocket
        PieSocketManager.sendMessage(text, chatId)
        updateChatPreview(chatId, text, true)
    }

    // Process incoming message from PieSocket
    private suspend fun processIncomingMessage(jsonStr: String) {
        val json = JSONObject(jsonStr)
        val chatId = json.optString("chatId")
        val text = json.optString("text")
        val isFromUser = json.optBoolean("isFromUser", false)

        // Save to Room only if it's a real reply (not echo)
        if (!isFromUser) {
            val msg = Message(
                chatId = chatId,
                text = text,
                isFromUser = false,
                isPending = false
            )
            messageDao.insert(msg)
        }

        updateChatPreview(chatId, text, isFromUser)
    }

    // Update preview + unread count
    private suspend fun updateChatPreview(chatId: String, lastMsg: String, isFromUser: Boolean) {
        _chatList.update { currentList ->
            currentList.map { chat ->
                if (chat.chatId == chatId) {
                    chat.copy(
                        lastMessage = lastMsg,
                        lastMessageTime = System.currentTimeMillis(),
                        unreadCount = if (!isFromUser) chat.unreadCount + 1 else 0
                    )
                } else chat
            }
        }
    }

    // Retry all pending messages when back online
    suspend fun retryPendingMessages() {
        if (!PieSocketManager.isOnline) return

        val pending = messageDao.getPendingMessages()
        pending.forEach { msg ->
            PieSocketManager.sendMessage(msg.text, msg.chatId)
            messageDao.markAsSent(msg.id)
        }
    }

    // Clear everything on app close (requirement)
    suspend fun clearAllData() {
        messageDao.clearAllMessages()
        _chatList.value = botList.map { it.copy(lastMessage = "", unreadCount = 0) }
    }
}