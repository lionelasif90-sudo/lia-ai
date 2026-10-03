package com.Lia.assistant.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.Lia.assistant.data.NovaAppState

/** Play: no accessibility disclosure route, no accessibility settings rows. */
fun NavGraphBuilder.registerAccessibilityDisclosureRoute(navController: NavController, appState: NovaAppState) {}

@Composable
fun AccessibilitySettingsRows(onOpenDisclosure: () -> Unit) {}
