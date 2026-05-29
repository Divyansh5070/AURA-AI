package com.chat.aichatbot.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chat.aichatbot.data.ApiConfig
import com.chat.aichatbot.data.ChatHistoryStore
import com.chat.aichatbot.data.ChatMessage
import com.chat.aichatbot.data.MessageRole
import com.chat.aichatbot.data.StaticChatResponder
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatHistoryStore = ChatHistoryStore(application)
    private val httpClient = OkHttpClient()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    private val _suggestions = MutableStateFlow<List<String>>(
        listOf(
            "💡 Explain quantum computing",
            "✍️ Write a welcome email",
            "💻 Kotlin coroutines example",
            "🥦 Suggest a healthy meal plan"
        )
    )
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    init {
        loadConversation()
    }

    /**
     * Load existing chat history from SharedPreferences.
     */
    private fun loadConversation() {
        viewModelScope.launch {
            val history = chatHistoryStore.loadHistory()
            _messages.value = history
            
            // If there's already an active history, hide suggestions.
            if (history.isNotEmpty()) {
                _suggestions.value = emptyList()
            }
        }
    }

    /**
     * Clear the full chat conversation and reset suggestions.
     */
    fun clearChat() {
        viewModelScope.launch {
            chatHistoryStore.clearHistory()
            _messages.value = emptyList()
            _suggestions.value = listOf(
                "💡 Explain quantum computing",
                "✍️ Write a welcome email",
                "💻 Kotlin coroutines example",
                "🥦 Suggest a healthy meal plan"
            )
        }
    }

    /**
     * Sends a user message and triggers a live Gemini response when configured.
     * If Gemini is not configured, the chat falls back to a static demo response.
     */
    fun sendMessage(text: String) {
        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            // 1. Add User Message to list
            val userMsg = ChatMessage(text = text.trim(), role = MessageRole.USER)
            val updatedList = _messages.value.toMutableList().apply { add(userMsg) }
            _messages.value = updatedList
            chatHistoryStore.saveHistory(updatedList)

            // Hide suggestions once chat begins
            _suggestions.value = emptyList()

            // 2. Trigger Typing Indicator
            _isTyping.value = true

            // 3. Resolve response text.
            val responseText: String
            if (ApiConfig.GEMINI_API_KEY.trim().isNotEmpty()) {
                val liveResponse = fetchGeminiResponse()
                _isTyping.value = false
                responseText = liveResponse.text ?: liveResponse.errorMessage
            } else {
                delay(500)
                _isTyping.value = false
                responseText = StaticChatResponder.responseFor(userMsg.text)
            }

            // 4. Stream Response in real-time (word by word) into the UI
            val aiMsgId = java.util.UUID.randomUUID().toString()
            val initialAiMsg = ChatMessage(id = aiMsgId, text = "", role = MessageRole.AI)
            
            // Insert empty AI message placeholder
            val withAiPlaceholder = _messages.value.toMutableList().apply { add(initialAiMsg) }
            _messages.value = withAiPlaceholder

            val words = responseText.split(" ")
            var currentText = ""

            for (i in words.indices) {
                currentText += (if (i == 0) "" else " ") + words[i]
                
                // Update the specifically matching AI message in state
                _messages.value = _messages.value.map { msg ->
                    if (msg.id == aiMsgId) msg.copy(text = currentText) else msg
                }
                
                // Pacing for word-by-word streaming
                delay(40)
            }

            // Save final updated list
            chatHistoryStore.saveHistory(_messages.value)
        }
    }

    /**
     * Executes HTTP POST query to the Gemini generateContent API with multi-turn conversation memory.
     */
    private suspend fun fetchGeminiResponse(): GeminiResult = suspendCancellableCoroutine { continuation ->
        val mediaType = "application/json; charset=utf-8".toMediaType()
        
        // Assemble conversation context history
        val contentsArray = JSONArray().apply {
            _messages.value.forEach { msg ->
                put(JSONObject().apply {
                    put("role", if (msg.role == MessageRole.USER) "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                })
            }
        }

        val jsonPayload = JSONObject().apply {
            put(
                "system_instruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            """
                            You are Aura AI, a careful and helpful assistant.
                            Prioritize factual accuracy over sounding confident.
                            If you are unsure, say so clearly and ask a clarifying question when needed.
                            Do not invent facts, links, sources, prices, dates, laws, or current events.
                            You do not have live internet access in this app unless the developer adds a search tool.
                            For recent or time-sensitive topics, explain that the answer may need verification.
                            Keep answers practical, clear, and directly relevant to the user's question.
                            """.trimIndent()
                        )
                    )
                )
            )
            put("contents", contentsArray)
            put(
                "generationConfig",
                JSONObject().put("temperature", 0.2)
            )
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/${ApiConfig.GEMINI_MODEL}:generateContent")
            .post(jsonPayload.toString().toRequestBody(mediaType))
            .addHeader("x-goog-api-key", ApiConfig.GEMINI_API_KEY.trim())
            .addHeader("Content-Type", "application/json")
            .build()

        val call = httpClient.newCall(request)
        continuation.invokeOnCancellation {
            call.cancel()
        }

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resume(GeminiResult(errorMessage = "⚠️ Could not reach Gemini. Please check your internet connection and try again."))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val bodyString = response.body?.string() ?: ""
                    if (!response.isSuccessful) {
                        continuation.resume(GeminiResult(errorMessage = response.toUserFacingError(bodyString)))
                        return
                    }
                    try {
                        val jsonResponse = JSONObject(bodyString)
                        val parts = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                        val content = buildString {
                            for (index in 0 until parts.length()) {
                                append(parts.getJSONObject(index).optString("text"))
                            }
                        }.trim()
                        continuation.resume(GeminiResult(text = content.ifBlank { null }))
                    } catch (e: Exception) {
                        continuation.resume(GeminiResult(errorMessage = "⚠️ Gemini responded, but the app could not read the response."))
                    }
                }
            }
        })
    }

    private fun Response.toUserFacingError(bodyString: String): String {
        val apiMessage = runCatching {
            JSONObject(bodyString)
                .optJSONObject("error")
                ?.optString("message")
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()

        return when (code) {
            400 -> apiMessage?.let { "⚠️ Gemini request error: $it" }
                ?: "⚠️ Gemini could not process this request."
            401 -> "⚠️ Gemini rejected the API key. Add a valid key to local.properties as GEMINI_API_KEY."
            403 -> "⚠️ This Gemini API key does not have access to the selected model or API."
            404 -> "⚠️ Gemini could not find the selected model: ${ApiConfig.GEMINI_MODEL}."
            429 -> "⚠️ Gemini free quota or rate limit reached. Please wait, reduce usage, or enable billing for higher limits."
            in 500..599 -> "⚠️ Gemini is temporarily unavailable. Please try again in a moment."
            else -> apiMessage?.let { "⚠️ Gemini error $code: $it" }
                ?: "⚠️ Gemini request failed with HTTP $code."
        }
    }

    private data class GeminiResult(
        val text: String? = null,
        val errorMessage: String = "⚠️ Failed to connect to Gemini."
    )

}
