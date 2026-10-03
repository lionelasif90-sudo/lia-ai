package com.Lia.assistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.VoiceSessionManager as V

@Composable
fun DebugScreen(nav: NavController) {
    val state by V.state.collectAsState()
    val active by V.isActive.collectAsState()
    val personality by V.currentPersonality.collectAsState()
    val language by V.currentLanguage.collectAsState()
    val detected by V.detectedLanguage.collectAsState()
    val vad by V.vadActive.collectAsState()
    val muted by V.muted.collectAsState()
    val error by V.lastError.collectAsState()
    val lat by V.latency.snapshot.collectAsState()
    val reconnects by V.latency.reconnectCount.collectAsState()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        NovaTopBar("Voice pipeline debugger", onBack = { nav.popBackStack() })
        NovaGlassCard(Modifier.padding(vertical = 4.dp)) {
            Column {
                val t = novaColors.textPrimary
                Text("Connection: $state (active=$active)", color = t)
                Text("Personality: ${personality.displayName}", color = t)
                Text("Language: ${language.displayName} / detected ${detected.name}", color = t)
                Text("VAD: $vad   Mic muted: $muted", color = t)
                Text("Reconnects: $reconnects", color = t)
                Text("Last error: ${error ?: "-"}", color = t)
                Text("Response start: ${lat.responseStartMs ?: "-"} ms", color = t)
                Text("First audio: ${lat.firstAudioMs ?: "-"} ms", color = t)
                Text("Turn duration: ${lat.turnDurationMs ?: "-"} ms", color = t)
            }
        }
    }
}
