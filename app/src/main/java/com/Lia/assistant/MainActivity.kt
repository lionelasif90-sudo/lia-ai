package com.Lia.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.Lia.assistant.data.NovaAppState
import com.Lia.assistant.ui.NovaApp
import com.Lia.assistant.ui.theme.NovaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appState = remember { NovaAppState(applicationContext) }
            NovaTheme(mode = appState.themeMode, reducedMotion = appState.reducedMotion) {
                NovaApp(appState)
            }
        }
    }
}
