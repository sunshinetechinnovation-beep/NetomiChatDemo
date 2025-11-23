package com.ssti.netomichatdemonogit.ui.theme.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssti.netomichatdemonogit.data.model.ChatItem
import com.ssti.netomichatdemonogit.ui.theme.viewmodel.ChatViewModel
import com.ssti.netomichatdemonogit.util.NetworkObserver
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val chatList by viewModel.chatList.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val isOnline by NetworkObserver.isOnline.collectAsState()  // ← REAL NETWORK!

    // Inside ChatListScreen composable
    val selectedBotName by viewModel.selectedBotName.collectAsState()   // ← ADD THIS LINE

    // THIS IS THE KEY FIX: Use derivedStateOf + fallback
    val selectedChatId by remember(chatList) {
        derivedStateOf {
            chatList.firstOrNull { it.chatId == (chatList.firstOrNull()?.chatId) }?.chatId
                ?: chatList.firstOrNull()?.chatId
                ?: "support"  // Final fallback
        }
    }

    LaunchedEffect(isOnline) {
        viewModel.toggleOnline(isOnline)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Netomi Chat Demo", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudQueue else Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = if (isOnline) Color.Green else Color.Red
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            // Main Chat List
            LazyColumn(modifier = Modifier.weight(1f)) {
                if (chatList.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Loading chats...", color = Color.Gray)
                        }
                    }
                } else {
                    items(chatList) { chat ->
                        ChatRow(
                            chat = chat,
                            isSelected = chat.chatId == selectedChatId,
                            onClick = { /* Optional: allow selection */ }
                        )
                        HorizontalDivider()
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Bot Selector
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedBotName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            chatList.forEach { chat ->
                                DropdownMenuItem(
                                    text = { Text(chat.botName) },
                                    onClick = {
                                        // Here we force update selectedChatId properly
                                        viewModel.selectBotName(chat.botName)   // ← Update ViewModel
                                        viewModel.selectBot(chat.chatId)   // ← Update ViewModel
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Type a message...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(Modifier.width(12.dp))

                        Button(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendMessage(selectedChatId, messageText.trim())
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                Color.Red
                            )
                        ) {
                            Text("Send", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRow(chat: ChatItem, isSelected: Boolean, onClick: () -> Unit) {
    val time = remember(chat.lastMessageTime) {
        if (chat.lastMessageTime == 0L) "No messages"
        else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(chat.lastMessageTime))
    }

    ListItem(
        headlineContent = {
            Text(chat.botName, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(
                text = chat.lastMessage.ifEmpty { "No messages yet" },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(time, fontSize = 12.sp, color = Color.Gray)
                if (chat.unreadCount > 0) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                        Text(chat.unreadCount.toString(), color = Color.White)
                    }
                }
            }
        },
        modifier = Modifier
            .clickable { onClick() }
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}