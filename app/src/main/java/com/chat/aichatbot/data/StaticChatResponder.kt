package com.chat.aichatbot.data

object StaticChatResponder {
    fun responseFor(prompt: String): String {
        val normalizedPrompt = prompt.lowercase()

        return when {
            normalizedPrompt.contains("quantum") ->
                "Quantum computing uses qubits instead of normal bits. A bit is either 0 or 1, but a qubit can represent a mix of both states, which helps certain problems run much faster than on regular computers."

            normalizedPrompt.contains("email") ->
                "Subject: Welcome!\n\nHi there,\n\nWelcome aboard. We are excited to have you with us and look forward to helping you get started. Please let us know if you have any questions.\n\nBest regards"

            normalizedPrompt.contains("coroutine") || normalizedPrompt.contains("kotlin") ->
                "In Kotlin, coroutines let you run asynchronous work without blocking the main thread. For example, a ViewModel can call viewModelScope.launch { ... } to fetch data, update state, and keep the UI responsive."

            normalizedPrompt.contains("meal") || normalizedPrompt.contains("healthy") ->
                "Here is a simple healthy meal idea: oatmeal with fruit for breakfast, dal or grilled paneer with rice and salad for lunch, fruit or nuts as a snack, and soup with vegetables or roti for dinner."

            normalizedPrompt.contains("hello") || normalizedPrompt.contains("hi") ->
                "Hello! I am ready to help. This chat screen supports messages, typing animation, scrolling, and saved conversation history."

            else ->
                "Thanks for your message. Gemini API is not configured in this build, so I am replying with a static demo response. The chat screen is still working with input, message bubbles, typing indicator, scrolling, and saved conversation history."
        }
    }
}
