package com.ssti.netomichatdemonogit.ui.theme.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssti.netomichatdemonogit.network.PieSocketManager
import com.ssti.netomichatdemonogit.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository
) : ViewModel() {

    val chatList = repository.chatList

    // THIS IS THE NEW CENTRAL SOURCE OF TRUTH
    private val _selectedBotId = MutableStateFlow("support")  // default bot
    val selectedBotId: StateFlow<String> = _selectedBotId.asStateFlow()

    private val _selectedBotName = MutableStateFlow("Support Bot")  // default bot
    val selectedBotName: StateFlow<String> = _selectedBotName.asStateFlow()

    init {
        PieSocketManager.connect()
    }

    fun selectBotName(botName: String) {
        _selectedBotName.value = botName
    }

    fun selectBot(botId: String) {
        _selectedBotId.value = botId
    }

    fun sendMessage(chatId: String, message: String) {
        viewModelScope.launch {
            repository.sendMessage(chatId, message)
        }
    }

    fun toggleOnline(isOnline: Boolean) {
        PieSocketManager.isOnline = isOnline
        if (isOnline) {
            viewModelScope.launch {
                repository.retryPendingMessages()
            }
        }
    }

    override fun onCleared() {
        viewModelScope.launch {
            repository.clearAllData()
            PieSocketManager.disconnect()
        }
        super.onCleared()
    }
}