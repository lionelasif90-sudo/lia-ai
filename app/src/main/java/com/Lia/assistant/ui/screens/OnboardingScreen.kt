package com.Lia.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.Lia.assistant.data.AssistantBrand
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.LiaOrb3D
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonStyle
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.NovaOrbState

private data class Page(val title: String, val body: String, val state: NovaOrbState)

@Composable
fun OnboardingScreen(appState: NovaAppState, onDone: () -> Unit) {
    val c = novaColors
    val pages = listOf(
        Page("Meet ${AssistantBrand.NAME}", "${AssistantBrand.TAGLINE}. Talk in Hindi, Hinglish or English.", NovaOrbState.IDLE),
        Page("Just talk", "She listens and answers in real time. You can interrupt her any time.", NovaOrbState.LISTENING),
        Page("Do things", "Open apps, call contacts and prepare messages with your voice.", NovaOrbState.SPEAKING),
        Page("Your key, your device", "Add your Gemini API key in Settings. It never leaves your phone except to talk to Gemini.", NovaOrbState.THINKING),
    )
    var i by remember { mutableIntStateOf(0) }
    fun finish() { appState.applyHasOnboarded(true); onDone() }
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(1f))
        LiaOrb3D(pages[i].state, size = 260.dp, style = appState.orbStyle)
        Spacer(Modifier.height(24.dp))
        Text(pages[i].title, style = MaterialTheme.typography.displayMedium, color = c.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(pages[i].body, color = c.textSecondary, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            NovaButton("Skip", { finish() }, style = NovaButtonStyle.TEXT)
            NovaButton(if (i == pages.lastIndex) "Get started" else "Next", { if (i == pages.lastIndex) finish() else i++ })
        }
    }
}
