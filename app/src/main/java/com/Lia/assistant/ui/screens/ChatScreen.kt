package com.Lia.assistant.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.ChatMessage
import com.Lia.assistant.ui.components.NovaMessageBubble
import com.Lia.assistant.ui.components.NovaOrb
import com.Lia.assistant.ui.components.NovaTypingDots
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.NovaShapes
import com.Lia.assistant.ui.theme.depthSurface
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.ChatPromptBuilder
import com.Lia.assistant.voice.ChatReplyResult
import com.Lia.assistant.voice.ChatTurn
import com.Lia.assistant.voice.GeminiTextClient
import com.Lia.assistant.voice.NovaOrbState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(appState: NovaAppState, nav: NavController) {
    val ctx = LocalContext.current
    val c = novaColors
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var nextId by remember { mutableStateOf(1L) }
    var recorded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // TextToSpeech created once, shut down on dispose.
    val tts = remember { arrayOfNulls<TextToSpeech>(1) }
    DisposableEffect(Unit) {
        tts[0] = TextToSpeech(ctx.applicationContext) { }
        onDispose { tts[0]?.shutdown(); tts[0] = null }
    }

    fun requestReply() {
        if (busy) return
        busy = true
        val history = messages.filter { !it.isError && !it.isStreaming }.map { ChatTurn(it.isUser, it.text) }
        val system = ChatPromptBuilder.build(appState.personality, appState.language, appState.assistantName)
        scope.launch {
            val result = GeminiTextClient.reply(ApiKeyStore.getKey(ctx), system, history)
            when (result) {
                is ChatReplyResult.Success -> {
                    val id = nextId++
                    messages.add(ChatMessage(id, "", false, isStreaming = true))
                    val idx = messages.lastIndex
                    val words = result.text.split(" ")
                    val sb = StringBuilder()
                    for (w in words) {
                        if (sb.isNotEmpty()) sb.append(' ')
                        sb.append(w)
                        messages[idx] = messages[idx].copy(text = sb.toString())
                        delay(20) // word-by-word reveal
                    }
                    messages[idx] = messages[idx].copy(isStreaming = false)
                }
                is ChatReplyResult.Error -> messages.add(ChatMessage(nextId++, result.message, false, isError = true))
            }
            busy = false
        }
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || busy) return
        input = ""
        if (!recorded) { recorded = true; appState.recordConversation(text) }
        messages.add(ChatMessage(nextId++, text, true))
        requestReply()
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(top = 28.dp), verticalAlignment = Alignment.CenterVertically) {
            NovaOrb(if (busy) NovaOrbState.THINKING else NovaOrbState.IDLE, size = 44.dp, style = appState.orbStyle, name = appState.assistantName)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(appState.assistantName, style = MaterialTheme.typography.headlineSmall, color = c.textPrimary)
                if (busy) NovaTypingDots() else Text("Here to help", style = MaterialTheme.typography.bodySmall, color = c.textSecondary)
            }
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(), state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(messages.size, key = { messages[it].id }) { i ->
                val m = messages[i]
                NovaMessageBubble(m, actions = {
                    IconButton({ clipboard.setText(AnnotatedString(m.text)) }, Modifier.size(48.dp)) {
                        Icon(Icons.Filled.ContentCopy, "Copy", tint = c.textSecondary)
                    }
                    IconButton({ tts[0]?.speak(m.text, TextToSpeech.QUEUE_FLUSH, null, "lia") }, Modifier.size(48.dp)) {
                        Icon(Icons.Filled.VolumeUp, "Speak", tint = c.textSecondary)
                    }
                    if (m.isError || i == messages.lastIndex) IconButton({
                        if (!busy) {
                            if (m.isError) messages.removeAt(i)
                            else messages.removeAt(i)
                            requestReply()
                        }
                    }, Modifier.size(48.dp)) { Icon(Icons.Filled.Refresh, "Retry", tint = c.textSecondary) }
                })
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                .padding(bottom = LocalBottomBarInset.current)
                .depthSurface(NovaShapes.pill, 10.dp).padding(start = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = input, onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message ${appState.assistantName}") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { send() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedTextColor = c.textPrimary, unfocusedTextColor = c.textPrimary, cursorColor = c.accent,
                ),
            )
            IconButton(onClick = { send() }, modifier = Modifier.size(48.dp).clip(CircleShape).background(c.accent)) {
                Icon(Icons.Filled.Send, "Send", tint = c.onAccent)
            }
        }
    }
}
