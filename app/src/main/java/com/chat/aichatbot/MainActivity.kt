package com.chat.aichatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.chat.aichatbot.ui.ChatScreen
import com.chat.aichatbot.ui.theme.ChatBotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatBotTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
