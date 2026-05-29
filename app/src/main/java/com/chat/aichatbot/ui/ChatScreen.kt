package com.chat.aichatbot.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chat.aichatbot.admob.AdBannerView
import com.chat.aichatbot.data.ChatMessage
import com.chat.aichatbot.data.MessageRole
import com.chat.aichatbot.viewmodel.ChatViewModel
import kotlinx.coroutines.delay

// ─── Minimal White Color System ──────────────────────────────────────────────
val BgPrimary      = Color(0xFFFFFFFF)   // page background
val BgSecondary    = Color(0xFFF7F7F7)   // subtle surface
val BgTertiary     = Color(0xFFEFEFEF)   // input / card bg
val BorderLight    = Color(0xFFE5E5E5)   // hairline borders
val BorderMedium   = Color(0xFFD0D0D0)   // slightly visible
val TextPrimary    = Color(0xFF111111)   // headings / body
val TextSecondary  = Color(0xFF777777)   // muted labels
val TextHint       = Color(0xFFB0B0B0)   // placeholders
val AccentBlack    = Color(0xFF111111)   // user bubble / send btn
val BubbleUser     = Color(0xFF111111)   // user message bg
val BubbleAi       = Color(0xFFF7F7F7)   // AI message bg
val OnlineGreen    = Color(0xFF22C55E)

private const val BannerCooldownMillis = 120_000L

// ─── Root Screen ─────────────────────────────────────────────────────────────
@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages    by chatViewModel.messages.collectAsState()
    val isTyping    by chatViewModel.isTyping.collectAsState()
    val suggestions by chatViewModel.suggestions.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState  = rememberLazyListState()
    val latestMessageText    = messages.lastOrNull()?.text.orEmpty()
    val bottomAnchorIndex    = messages.size + if (isTyping) 1 else 0

    var showInlineAd         by rememberSaveable { mutableStateOf(true) }
    var inlineAdDismissCount by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(showInlineAd, inlineAdDismissCount) {
        if (!showInlineAd) { delay(BannerCooldownMillis); showInlineAd = true }
    }
    LaunchedEffect(messages.size, isTyping) {
        if (messages.isNotEmpty() || isTyping) listState.animateToConversationEnd(bottomAnchorIndex)
    }
    LaunchedEffect(latestMessageText) {
        if (messages.lastOrNull()?.role == MessageRole.AI) listState.scrollToConversationEnd(bottomAnchorIndex)
    }

    Scaffold(
        topBar = {
            AuraChatTopBar(hasMessages = messages.isNotEmpty(), onClearChat = chatViewModel::clearChat)
        },
        bottomBar = {
            ChatBottomBar(
                inputText         = inputText,
                onInputTextChange = { inputText = it },
                onSendMessage     = { chatViewModel.sendMessage(inputText); inputText = "" }
            )
        },
        containerColor = BgPrimary
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgPrimary)
        ) {
            if (messages.isEmpty() && suggestions.isNotEmpty()) {
                WelcomeScreen(suggestions = suggestions) { suggestion ->
                    chatViewModel.sendMessage(suggestion)
                }
            } else {
                LazyColumn(
                    state               = listState,
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        MessageBubble(message = msg)
                    }

                    if (showInlineAd && messages.size >= 2) {
                        item(key = "inline_ad") {
                            InlineAdCard(onDismiss = {
                                showInlineAd = false
                                inlineAdDismissCount += 1
                            })
                        }
                    }

                    if (isTyping) {
                        item(key = "typing_indicator") {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                                contentAlignment = Alignment.CenterStart
                            ) { TypingIndicator() }
                        }
                    }

                    item(key = "bottom_anchor") { Spacer(Modifier.height(2.dp)) }
                }
            }
        }
    }
}

// ─── Welcome Screen ───────────────────────────────────────────────────────────
@Composable
private fun WelcomeScreen(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally
    ) {
        Text(
            text       = "Aura AI",
            fontSize   = 26.sp,
            fontWeight = FontWeight.Bold,
            color      = TextPrimary,
            modifier   = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text      = "Ask me anything to get started.",
            fontSize  = 14.sp,
            color     = TextSecondary,
            textAlign = TextAlign.Center,
            modifier  = Modifier.padding(bottom = 32.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgPrimary)
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                ) {
                    Text(text = suggestion, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

// ─── Bottom Bar ───────────────────────────────────────────────────────────────
@Composable
private fun ChatBottomBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Surface(color = BgPrimary, tonalElevation = 0.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .border(width = 0.5.dp, color = BorderLight, shape = RoundedCornerShape(0.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Input field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(BgTertiary)
                        .border(1.dp, BorderLight, RoundedCornerShape(22.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    BasicTextField(
                        value         = inputText,
                        onValueChange = onInputTextChange,
                        modifier      = Modifier.fillMaxWidth(),
                        textStyle     = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush   = SolidColor(TextPrimary),
                        decorationBox = { inner ->
                            if (inputText.isEmpty()) {
                                Text("Message Aura…", color = TextHint, fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                }

                // Send button
                val isActive = inputText.trim().isNotEmpty()
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isActive) AccentBlack else BgTertiary)
                        .border(1.dp, if (isActive) Color.Transparent else BorderLight, CircleShape)
                        .clickable(enabled = isActive) { onSendMessage() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint               = if (isActive) BgPrimary else TextHint,
                        modifier           = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

// ─── Inline Ad Card ───────────────────────────────────────────────────────────
@Composable
fun InlineAdCard(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(true) }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(220)) + expandVertically(tween(240, easing = EaseOutCubic)),
        exit    = fadeOut(tween(180)) + shrinkVertically(tween(200, easing = EaseInCubic))
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BgPrimary)
                .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
        ) {
            // Header: label + dismiss
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 8.dp, top = 9.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text          = "Sponsored",
                    fontSize      = 10.sp,
                    fontWeight    = FontWeight.Medium,
                    color         = TextHint,
                    letterSpacing = 0.4.sp
                )
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable { visible = false; onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Close,
                        contentDescription = "Dismiss ad",
                        tint               = TextHint,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }

            // Hairline divider
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(BorderLight)
            )

            // Ad creative
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                AdBannerView()
            }
        }
    }
}

// ─── Markdown Text ────────────────────────────────────────────────────────────
@Composable
private fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    isUser: Boolean = false
) {
    val baseColor = if (isUser) BgPrimary else TextPrimary
    val annotated = remember(text) {
        buildAnnotatedString {
            text.lines().forEachIndexed { idx, raw ->
                val isBullet = raw.trimStart().startsWith("* ") || raw.trimStart().startsWith("- ")
                val line     = if (isBullet) "  • " + raw.trimStart().removePrefix("* ").removePrefix("- ") else raw
                line.split("**").forEachIndexed { i, part ->
                    if (i % 2 == 0) append(part)
                    else { pushStyle(SpanStyle(fontWeight = FontWeight.SemiBold)); append(part); pop() }
                }
                if (idx < text.lines().lastIndex) append("\n")
            }
        }
    }
    Text(
        text       = annotated,
        color      = baseColor,
        fontSize   = 14.sp,
        lineHeight = 21.sp,
        modifier   = modifier
    )
}

// ─── Message Bubble ───────────────────────────────────────────────────────────
@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(260)) + slideInVertically(
            initialOffsetY = { 16 },
            animationSpec  = tween(260, easing = EaseOutQuad)
        )
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            val shape = RoundedCornerShape(
                topStart    = 18.dp,
                topEnd      = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd   = if (isUser) 4.dp else 18.dp
            )
            Box(
                modifier = Modifier
                    .widthIn(max = 272.dp)
                    .clip(shape)
                    .background(if (isUser) BubbleUser else BubbleAi)
                    .then(
                        if (!isUser) Modifier.border(1.dp, BorderLight, shape) else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                MarkdownText(text = message.text, isUser = isUser)
            }
        }
    }
}

// ─── Typing Indicator ─────────────────────────────────────────────────────────
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(BubbleAi)
            .border(1.dp, BorderLight, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val transition = rememberInfiniteTransition(label = "typing")
        (0..2).forEach { i ->
            val alpha by transition.animateFloat(
                initialValue  = 0.25f,
                targetValue   = 1f,
                animationSpec = infiniteRepeatable(
                    animation          = tween(550, easing = LinearEasing),
                    repeatMode         = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(i * 140)
                ),
                label = "dot$i"
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(TextPrimary.copy(alpha = alpha))
            )
        }
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────
@Composable
private fun AuraChatTopBar(
    hasMessages: Boolean,
    onClearChat: () -> Unit
) {
    Surface(color = BgPrimary, shadowElevation = 0.dp, tonalElevation = 0.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar with green ring + online dot
                Box(
                    modifier         = Modifier.size(46.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Green ring
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, OnlineGreen, CircleShape)
                    )
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = "AI",
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color      = BgPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    // Online dot
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(OnlineGreen)
                            .border(2.dp, BgPrimary, CircleShape)
                    )
                }

                // Name + status
                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text       = "Aura AI",
                        fontSize   = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                        lineHeight = 18.sp
                    )
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OnlineGreen)
                        )
                        Text(
                            text       = "Active now",
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color      = OnlineGreen
                        )
                    }
                }

                // Right actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (hasMessages) {
                        TopBarIconButton("Clear chat", onClearChat) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint     = TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }

                }
            }
            // Hairline divider
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(BorderLight)
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    contentDescription: String,
    onClick: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit
) {
    IconButton(
        onClick  = onClick,
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(BgPrimary)
            .border(1.dp, BorderLight, CircleShape)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center, content = content)
    }
}

// ─── Scroll helpers ───────────────────────────────────────────────────────────
private suspend fun LazyListState.animateToConversationEnd(idx: Int) {
    if (idx >= 0) animateScrollToItem(idx)
}
private suspend fun LazyListState.scrollToConversationEnd(idx: Int) {
    if (idx >= 0) scrollToItem(idx)
}