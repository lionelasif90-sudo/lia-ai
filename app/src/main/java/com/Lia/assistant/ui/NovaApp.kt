package com.Lia.assistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaOrb
import com.Lia.assistant.ui.screens.ChatScreen
import com.Lia.assistant.ui.screens.DebugScreen
import com.Lia.assistant.ui.screens.HistoryScreen
import com.Lia.assistant.ui.screens.HomeScreen
import com.Lia.assistant.ui.screens.OnboardingScreen
import com.Lia.assistant.ui.screens.OrbStyleScreen
import com.Lia.assistant.ui.screens.PersonalityScreen
import com.Lia.assistant.ui.screens.SettingsScreen
import com.Lia.assistant.ui.screens.VoiceScreen
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.depthSurface
import com.Lia.assistant.ui.theme.NovaShapes
import com.Lia.assistant.ui.theme.nightSky
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.NovaOrbState
import com.Lia.assistant.voice.VoiceSessionManager

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val CHAT = "chat"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val VOICE = "voice"
    const val PERSONALITY = "personality"
    const val ORB_STYLE = "orb_style"
    const val DEBUG = "debug"
}

private val BAR_ROUTES = setOf(Routes.HOME, Routes.CHAT, Routes.HISTORY, Routes.SETTINGS)

@Composable
fun NovaApp(appState: NovaAppState) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = route in BAR_ROUTES

    CompositionLocalProvider(LocalBottomBarInset provides if (showBar) 96.dp else 0.dp) {
        Box(Modifier.fillMaxSize().nightSky()) {
            NavHost(
                nav,
                startDestination = if (appState.hasOnboarded) Routes.HOME else Routes.ONBOARDING,
                modifier = Modifier.fillMaxSize(),
            ) {
                composable(Routes.ONBOARDING) { OnboardingScreen(appState) { nav.navigateTop(Routes.HOME) } }
                composable(Routes.HOME) { HomeScreen(appState, nav) }
                composable(Routes.CHAT) { ChatScreen(appState, nav) }
                composable(Routes.HISTORY) { HistoryScreen(appState, nav) }
                composable(Routes.SETTINGS) { SettingsScreen(appState, nav) }
                composable(Routes.VOICE) { VoiceScreen(appState, nav) }
                composable(Routes.PERSONALITY) { PersonalityScreen(appState, nav) }
                composable(Routes.ORB_STYLE) { OrbStyleScreen(appState, nav) }
                composable(Routes.DEBUG) { DebugScreen(nav) }
                registerAccessibilityDisclosureRoute(nav, appState)
            }
            if (showBar) {
                LiaBottomNavigation(
                    current = route.orEmpty(), appState = appState,
                    onNavigate = { nav.navigateTop(it) },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

private fun NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { inclusive = false; saveState = true }
        launchSingleTop = true
        restoreState = route != Routes.VOICE
    }
}

@Composable
private fun LiaBottomNavigation(
    current: String, appState: NovaAppState, onNavigate: (String) -> Unit, modifier: Modifier = Modifier,
) {
    val c = novaColors
    val state by VoiceSessionManager.state.collectAsState()
    Box(modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
            Modifier.fillMaxWidth().height(64.dp).depthSurface(NovaShapes.pill, 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(Icons.Filled.Home, "Home", current == Routes.HOME, Modifier.weight(1f)) { onNavigate(Routes.HOME) }
            NavItem(Icons.Filled.Chat, "Chat", current == Routes.CHAT, Modifier.weight(1f)) { onNavigate(Routes.CHAT) }
            Spacer(Modifier.width(72.dp))
            NavItem(Icons.Filled.History, "History", current == Routes.HISTORY, Modifier.weight(1f)) { onNavigate(Routes.HISTORY) }
            NavItem(Icons.Filled.Settings, "Settings", current == Routes.SETTINGS, Modifier.weight(1f)) { onNavigate(Routes.SETTINGS) }
        }
        Box(
            Modifier.align(Alignment.TopCenter).padding(top = 0.dp).size(64.dp).clip(CircleShape)
                .background(c.surfaceRaised)
                .clickable(role = Role.Button, onClickLabel = "Talk") { onNavigate(Routes.VOICE) },
            contentAlignment = Alignment.Center,
        ) {
            NovaOrb(
                state = if (state == NovaOrbState.IDLE) NovaOrbState.IDLE else state,
                size = 64.dp, style = appState.orbStyle, name = appState.assistantName,
            )
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = novaColors
    Column(
        modifier.height(64.dp).clickable(role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (selected) c.accent else c.textSecondary)
        Text(label, color = if (selected) c.accent else c.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
}
