package com.Lia.assistant.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.LanguagePreference
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.AccessibilitySettingsRows
import com.Lia.assistant.ui.Routes
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonStyle
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaSectionHeader
import com.Lia.assistant.ui.components.NovaSettingsRow
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.NovaShapes
import com.Lia.assistant.ui.theme.ThemeMode
import com.Lia.assistant.ui.theme.novaColors

private val VOICE_CHOICES = listOf("Aoede", "Puck", "Charon", "Kore", "Fenrir", "Leda", "Orus", "Zephyr")

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    val c = novaColors
    Box(
        Modifier.heightIn(min = 48.dp).clip(NovaShapes.pill)
            .background(if (selected) c.accent else c.surfaceRaised)
            .clickable(onClick = onClick).padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, color = if (selected) c.onAccent else c.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelLarge) }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(appState: NovaAppState, nav: NavController) {
    val ctx = LocalContext.current
    val c = novaColors
    var keyInput by remember { mutableStateOf("") }
    var keySaved by remember { mutableStateOf(ApiKeyStore.hasKey(ctx)) }
    var nameInput by remember { mutableStateOf(appState.assistantName) }
    var userInput by remember { mutableStateOf(appState.userName) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
        .padding(bottom = LocalBottomBarInset.current)) {
        NovaTopBar("Settings")

        NovaSectionHeader("Gemini API key")
        NovaGlassCard(Modifier.fillMaxWidth()) {
            Column {
                Text(if (keySaved) "A key is saved on this device." else "Paste your Gemini API key. It stays on this device.",
                    color = c.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                NovaTextField(keyInput, { keyInput = it }, "API key", secret = true)
                Spacer(Modifier.height(8.dp))
                NovaButton("Save key", {
                    ApiKeyStore.saveKey(ctx, keyInput); keyInput = ""; keySaved = ApiKeyStore.hasKey(ctx)
                }, enabled = keyInput.isNotBlank())
            }
        }

        NovaSectionHeader("Assistant")
        NovaGlassCard(Modifier.fillMaxWidth()) {
            Column {
                NovaTextField(nameInput, { nameInput = it }, "Assistant name")
                Spacer(Modifier.height(8.dp))
                NovaButton("Rename", { appState.applyAssistantName(nameInput); nameInput = appState.assistantName },
                    style = NovaButtonStyle.SECONDARY)
                Spacer(Modifier.height(12.dp))
                NovaTextField(userInput, { userInput = it }, "Your name")
                Spacer(Modifier.height(8.dp))
                NovaButton("Save your name", { appState.applyUserName(userInput) }, style = NovaButtonStyle.SECONDARY)
                Spacer(Modifier.height(8.dp))
                NovaSettingsRow(Icons.Filled.Psychology, "Personality", value = appState.personality.displayName,
                    onClick = { nav.navigate(Routes.PERSONALITY) })
            }
        }

        NovaSectionHeader("Language")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LanguagePreference.entries.forEach { l -> Chip(l.displayName, appState.language == l) { appState.applyLanguage(l) } }
        }
        Text(appState.language.description, color = c.textTertiary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp))

        NovaSectionHeader("Voice")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VOICE_CHOICES.forEach { v -> Chip(v, appState.voiceName == v) { appState.applyVoiceName(v) } }
        }
        Spacer(Modifier.height(12.dp))
        Text("Volume", color = c.textSecondary)
        Slider(appState.voiceVolume, { appState.applyVoiceVolume(it) }, valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = c.accent, activeTrackColor = c.accent))

        NovaSectionHeader("Appearance")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeMode.entries.forEach { m ->
                Chip(m.name.lowercase().replaceFirstChar { it.uppercase() }, appState.themeMode == m) { appState.applyThemeMode(m) }
            }
        }
        NovaSettingsRow(Icons.Filled.Palette, "Reduced motion", "Freeze animations and tilt",
            checked = appState.reducedMotion, onCheckedChange = { appState.applyReducedMotion(it) })
        NovaSettingsRow(Icons.Filled.Face, "Orb style", value = appState.orbStyle.displayName, onClick = { nav.navigate(Routes.ORB_STYLE) })

        NovaSectionHeader("Phone control")
        AccessibilitySettingsRows(onOpenDisclosure = { nav.navigate("accessibility_disclosure") })

        NovaSectionHeader("Developer")
        NovaSettingsRow(Icons.Filled.BugReport, "Voice pipeline debugger", onClick = { nav.navigate(Routes.DEBUG) })
        Spacer(Modifier.height(24.dp))
    }
}
