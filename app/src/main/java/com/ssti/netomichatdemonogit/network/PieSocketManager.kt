package com.ssti.netomichatdemonogit.network

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

object PieSocketManager {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private val _incomingMessage = MutableStateFlow<String?>(null)
    val incomingMessage: StateFlow<String?> = _incomingMessage

    // UPDATE THESE WITH YOUR FREE KEY FROM STEP 1
    private const val API_KEY = "1NbpJnY7Sli4NGRlFGx8bR5bjUrC66gxnnYL7niv"  // e.g., "abc123def456"
    private const val APP_ID = "20111"    // e.g., "123456"
    private const val CLUSTER_ID = "s15465.blr1"
    private const val CHANNEL = "1"     // Your private channel

    private const val WS_URL = "wss://$CLUSTER_ID.piesocket.com/v3/$CHANNEL?app_id=$APP_ID&api_key=$API_KEY&notify_self=1"

    var isOnline = true
        set(value) {
            field = value
            if (value && webSocket == null) connect()
        }

    fun connect() {
        val request = Request.Builder().url(WS_URL).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("PieSocket", "✅ Connected successfully to PieSocket!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("PieSocket", "📨 Received: $text")
                _incomingMessage.tryEmit(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                onMessage(webSocket, bytes.utf8())
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.w("PieSocket", "Closing: $reason")
                webSocket.close(1000, null)
                this@PieSocketManager.webSocket = null
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("PieSocket", "❌ Failed: ${t.message}")
                this@PieSocketManager.webSocket = null
            }
        })
    }

    fun sendMessage(text: String, chatId: String) {
        if (!isOnline) {
            Log.d("", "📱 Offline → message queued locally")
            return
        }

        val json = JSONObject().apply {
            put("chatId", chatId)
            put("text", text)
            put("isFromUser", true)
        }

        if (webSocket == null) {
            connect()
            GlobalScope.launch {
                delay(2500)  // Wait for connect
                webSocket?.send(json.toString()) ?: Log.e("PieSocket", "Send failed - no WS")
            }
        } else {
            webSocket?.send(json.toString()) ?: Log.e("PieSocket", "Send failed - WS null")
        }
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        webSocket = null
    }
}