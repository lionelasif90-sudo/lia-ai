package com.Lia.assistant.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaOrb
import com.Lia.assistant.ui.components.NovaOrbStyle
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.voice.NovaOrbState

@Composable
fun OrbStyleScreen(appState: NovaAppState, nav: NavController) {
    var preview by remember { mutableStateOf(NovaOrbState.IDLE) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        NovaTopBar("Orb style", onBack = { nav.popBackStack() })
        Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            NovaOrb(preview, size = 260.dp, style = appState.orbStyle, amplitude = if (preview == NovaOrbState.SPEAKING) 0.5f else 0f,
                name = appState.assistantName)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(NovaOrbState.IDLE to "Idle", NovaOrbState.LISTENING to "Listening", NovaOrbState.SPEAKING to "Speaking").forEach { (s, label) ->
                Chip(label, preview == s) { preview = s }
            }
        }
        Text("Style", modifier = Modifier.padding(top = 24.dp, bottom = 8.dp), color = com.Lia.assistant.ui.theme.novaColors.textSecondary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(NovaOrbStyle.entries.toList()) { s -> Chip(s.displayName, appState.orbStyle == s) { appState.applyOrbStyle(s) } }
        }
    }
}
