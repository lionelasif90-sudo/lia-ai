package com.Lia.assistant.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.Lia.assistant.data.ApiKeyStore
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.Routes
import com.Lia.assistant.ui.components.LiaOrb3D
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonStyle
import com.Lia.assistant.ui.components.NovaVoiceVisualizer
import com.Lia.assistant.ui.theme.NovaVoiceItalic
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.NovaOrbState
import com.Lia.assistant.voice.VoiceForegroundService
import com.Lia.assistant.voice.VoiceSessionManager

@Composable
fun VoiceScreen(appState: NovaAppState, nav: NavController) {
    val ctx = LocalContext.current
    val c = novaColors
    val state by VoiceSessionManager.state.collectAsState()
    val amp by VoiceSessionManager.amplitude.collectAsState()
    val status by VoiceSessionManager.statusText.collectAsState()
    val muted by VoiceSessionManager.muted.collectAsState()
    val active by VoiceSessionManager.isActive.collectAsState()
    val error by VoiceSessionManager.lastError.collectAsState()

    var hasMic by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    val hasKey = ApiKeyStore.hasKey(ctx)

    fun startService() {
        ContextCompat.startForegroundService(ctx, Intent(ctx, VoiceForegroundService::class.java))
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { startService() }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasMic = granted
        if (granted) {
            if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else startService()
        }
    }

    // Permission chain: mic -> notifications -> start. The service is NOT stopped on dispose.
    LaunchedEffect(hasKey) {
        if (hasKey && !active) {
            if (!hasMic) micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            else if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            else startService()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            val shown = if (!hasKey || !hasMic) NovaOrbState.ERROR else state
            Box(contentAlignment = Alignment.Center) {
                if (state == NovaOrbState.SPEAKING) NovaVoiceVisualizer(amp, size = 320.dp)
                LiaOrb3D(shown, size = 240.dp, amplitude = amp, style = appState.orbStyle, name = appState.assistantName)
            }
            Spacer(Modifier.height(20.dp))
            val message = when {
                !hasKey -> "Add your Gemini API key in Settings to start."
                !hasMic -> "${appState.assistantName} needs the microphone to listen."
                state == NovaOrbState.ERROR -> error ?: status
                else -> status.ifBlank { "Starting..." }
            }
            Text(message, style = NovaVoiceItalic, color = if (shown == NovaOrbState.ERROR) c.error else c.textPrimary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            if (!hasKey) NovaButton("Open Settings", { nav.navigate(Routes.SETTINGS) })
            else if (!hasMic) NovaButton("Allow microphone", { micLauncher.launch(Manifest.permission.RECORD_AUDIO) })
            else if (!active) NovaButton("Start", { startService() })
            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
                RoundControl(if (muted) Icons.Filled.MicOff else Icons.Filled.Mic, if (muted) "Unmute" else "Mute", 56.dp, c.surfaceRaised, c.textPrimary) {
                    VoiceSessionManager.toggleMute()
                }
                RoundControl(Icons.Filled.Stop, "Stop", 72.dp, c.error.copy(alpha = 0.25f), c.error) {
                    ctx.startService(Intent(ctx, VoiceForegroundService::class.java).setAction(VoiceForegroundService.ACTION_STOP))
                }
                RoundControl(Icons.Filled.Close, "Close", 56.dp, c.surfaceRaised, c.textPrimary) { nav.popBackStack() }
            }
        }
    }
}

@Composable
private fun RoundControl(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, size: androidx.compose.ui.unit.Dp,
                         bg: androidx.compose.ui.graphics.Color, tint: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    IconButton(onClick, Modifier.size(size).clip(CircleShape).background(bg)) {
        Icon(icon, contentDescription = label, tint = tint)
    }
}
