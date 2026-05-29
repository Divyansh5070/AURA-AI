package com.chat.aichatbot.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ChatHistoryStore(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("ai_chatbot_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CHAT_HISTORY = "key_chat_history"
    }

    /**
     * Persists the conversation history to local storage.
     */
    fun saveHistory(messages: List<ChatMessage>) {
        val json = gson.toJson(messages)
        sharedPreferences.edit().putString(KEY_CHAT_HISTORY, json).apply()
    }

    /**
     * Loads the conversation history from local storage.
     * Returns an empty list if no history has been saved.
     */
    fun loadHistory(): List<ChatMessage> {
        val json = sharedPreferences.getString(KEY_CHAT_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<ChatMessage>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clears all saved conversation history.
     */
    fun clearHistory() {
        sharedPreferences.edit().remove(KEY_CHAT_HISTORY).apply()
    }
}
