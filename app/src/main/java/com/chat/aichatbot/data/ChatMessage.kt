package com.chat.aichatbot.data

import java.util.UUID

enum class MessageRole {
    USER, AI
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val role: MessageRole,
    val timestamp: Long = System.currentTimeMillis()
)
