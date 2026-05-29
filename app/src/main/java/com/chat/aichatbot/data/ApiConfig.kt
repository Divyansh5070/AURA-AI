package com.chat.aichatbot.data

import com.chat.aichatbot.BuildConfig

object ApiConfig {
    /**
     * Put your Gemini API key in local.properties:
     * GEMINI_API_KEY=AIza...
     * GEMINI_MODEL=gemini-2.5-flash
     *
     * IMPORTANT: If this key is left blank/empty, the application will automatically
     * use static demo responses so the chat screen still works without live AI.
     */
    val GEMINI_API_KEY: String = BuildConfig.GEMINI_API_KEY
    val GEMINI_MODEL: String = BuildConfig.GEMINI_MODEL
}
