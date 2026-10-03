package com.Lia.assistant.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.Routes
import com.Lia.assistant.ui.components.NovaEmptyState
import com.Lia.assistant.ui.components.NovaGlassCard
import com.Lia.assistant.ui.components.NovaTextField
import com.Lia.assistant.ui.components.NovaTopBar
import com.Lia.assistant.ui.theme.LocalBottomBarInset
import com.Lia.assistant.ui.theme.novaColors
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(appState: NovaAppState, nav: NavController) {
    var query by remember { mutableStateOf("") }
    val filtered = appState.conversations.filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        NovaTopBar("History")
        NovaTextField(query, { query = it }, "Search")
        if (filtered.isEmpty()) {
            NovaEmptyState("No conversations yet", "Start chatting", { nav.navigate(Routes.CHAT) })
        } else {
            LazyColumn(Modifier.fillMaxWidth().padding(top = 12.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = LocalBottomBarInset.current)) {
                items(filtered, key = { it.id }) { conv ->
                    NovaGlassCard(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { nav.navigate(Routes.CHAT) }) {
                        Column {
                            Text(conv.title, color = novaColors.textPrimary)
                            Text(DateFormat.getDateTimeInstance().format(Date(conv.updatedAt)), color = novaColors.textTertiary,
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
