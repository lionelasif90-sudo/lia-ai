package com.Lia.assistant.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.data.Personality
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.LocalNovaReducedMotion
import com.Lia.assistant.ui.theme.NovaShapes
import com.Lia.assistant.ui.theme.novaColors

@Composable
fun PersonalityScreen(appState: NovaAppState, nav: NavController) {
    val c = novaColors
    val reduced = LocalNovaReducedMotion.current
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        NovaTopBar("Personality", onBack = { nav.popBackStack() })
        Text("Changes take effect immediately", color = c.textSecondary, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
        LazyVerticalGrid(
            GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Personality.entries.toList(), key = { it.name }) { p ->
                val selected = appState.personality == p
                val s by animateFloatAsState(if (selected && !reduced) 1.04f else 1f, label = "lift")
                NovaGlassCard(
                    Modifier.fillMaxWidth().scale(s).alpha(if (selected) 1f else 0.75f)
                        .let { if (selected) it.border(2.dp, c.accent, NovaShapes.medium) else it },
                    onClick = { appState.applyPersonality(p) },
                ) {
                    Column {
                        Text(p.icon, style = MaterialTheme.typography.headlineSmall)
                        Text(p.displayName, color = c.textPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(p.description, color = c.textSecondary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
