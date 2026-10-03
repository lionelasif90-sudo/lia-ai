package com.Lia.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.Routes
import com.Lia.assistant.ui.components.LiaOrb3D
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonStyle
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.NovaVoiceItalic
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.VoiceSessionManager
import java.util.Calendar

@Composable
fun HomeScreen(appState: NovaAppState, nav: NavController) {
    val c = novaColors
    val state by VoiceSessionManager.state.collectAsState()
    val amp by VoiceSessionManager.amplitude.collectAsState()
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greet = when { hour < 5 -> "Good night"; hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }
    val who = appState.userName.takeIf { it.isNotBlank() }?.let { ", $it" }.orEmpty()
    val tries = listOf("YouTube kholo", "Mummy ko call karo", "Rahul ko WhatsApp karo", "Tell me a joke")

    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = LocalBottomBarInset.current),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        Text("$greet$who", style = MaterialTheme.typography.displayMedium, color = c.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Box(Modifier.clickable { nav.navigate(Routes.VOICE) }) {
            LiaOrb3D(state, size = 260.dp, amplitude = amp, style = appState.orbStyle, name = appState.assistantName)
        }
        Text("Tap me to talk", style = NovaVoiceItalic, color = c.textSecondary)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NovaButton("Talk", { nav.navigate(Routes.VOICE) })
            NovaButton("Type", { nav.navigate(Routes.CHAT) }, style = NovaButtonStyle.SECONDARY)
        }
        Spacer(Modifier.height(28.dp))
        Text("Try saying", color = c.textTertiary, style = MaterialTheme.typography.labelMedium, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        tries.forEach { phrase ->
            NovaGlassCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.VOICE) }) {
                Text("“$phrase”", color = c.textPrimary, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
