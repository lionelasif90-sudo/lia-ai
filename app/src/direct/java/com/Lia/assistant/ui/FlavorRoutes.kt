package com.Lia.assistant.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.components.NovaButton
import com.Lia.assistant.ui.components.NovaButtonStyle
import com.Lia.assistant.ui.components.NovaSettingsRow
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.novaColors

const val ACCESSIBILITY_DISCLOSURE_ROUTE = "accessibility_disclosure"

fun NavGraphBuilder.registerAccessibilityDisclosureRoute(navController: NavController, appState: NovaAppState) {
    composable(ACCESSIBILITY_DISCLOSURE_ROUTE) {
        val ctx: Context = LocalContext.current
        Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            NovaTopBar(title = "Accessibility access", onBack = { navController.popBackStack() })
            Text("${appState.assistantName} uses Android's Accessibility service only when you ask her to act on your phone.",
                color = novaColors.textPrimary)
            Spacer(Modifier.height(12.dp))
            Text("What it can do: read the text and buttons on the current screen, tap, type and scroll.\n\n" +
                "What it does not do: it does not run in the background on its own, never reads passwords, " +
                "and never sends, posts or pays without your explicit yes. Nothing from your screen is stored or uploaded " +
                "except what is needed to answer your current request.\n\nYou can turn it off any time in Android settings.",
                color = novaColors.textSecondary)
            Spacer(Modifier.height(20.dp))
            NovaButton("Open Accessibility settings", style = NovaButtonStyle.PRIMARY, onClick = {
                ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            })
        }
    }
}

@Composable
fun AccessibilitySettingsRows(onOpenDisclosure: () -> Unit) {
    val ctx = LocalContext.current
    val on = com.Lia.assistant.action.NovaAccessibilityService.isEnabled(ctx)
    NovaSettingsRow(
        icon = Icons.Filled.Accessibility,
        title = "Screen control",
        subtitle = "Read, tap, type and scroll when you ask",
        value = if (on) "On" else "Off",
        onClick = onOpenDisclosure,
    )
}
