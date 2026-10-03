package com.Lia.assistant.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

enum class ThemeMode {
    DARK, LIGHT, SYSTEM;
    companion object { fun fromName(n: String?) = entries.firstOrNull { it.name == n } ?: DARK }
}

data class NovaColorScheme(
    val background: Color, val backgroundGradientTop: Color, val surface: Color, val surfaceGlass: Color,
    val surfaceBorder: Color, val surfaceRaised: Color, val accent: Color, val accentSecondary: Color,
    val accentGlow: Color, val onAccent: Color, val textPrimary: Color, val textSecondary: Color,
    val textTertiary: Color, val success: Color, val warning: Color, val error: Color,
    val userBubble: Color, val assistantBubble: Color, val depthShadow: Color, val rimLight: Color,
    val lagoon: Color, val lotus: Color,
)

val NovaDark = NovaColorScheme(
    background = Color(0xFF06050E), backgroundGradientTop = Color(0xFF15113A),
    surface = Color(0xFF17143A), surfaceGlass = Color(0xB317143A), surfaceBorder = Color(0x33FFFFFF),
    surfaceRaised = Color(0xFF221E4D), accent = Color(0xFFFFB547), accentSecondary = Color(0xFFFF8A3D),
    accentGlow = Color(0x66FFB547), onAccent = Color(0xFF1A1000), textPrimary = Color(0xFFEEE9FF),
    textSecondary = Color(0xFFA7A0CC), textTertiary = Color(0xFF7A74A3), success = Color(0xFF3FE0D0),
    warning = Color(0xFFFFB547), error = Color(0xFFFF7A85), userBubble = Color(0x40FFB547),
    assistantBubble = Color(0xB317143A), depthShadow = Color(0xFF6C4DFF), rimLight = Color(0x66FFFFFF),
    lagoon = Color(0xFF3FE0D0), lotus = Color(0xFFFF6FAE),
)

val NovaLight = NovaColorScheme(
    background = Color(0xFFF3F0FB), backgroundGradientTop = Color(0xFFE4DDFB),
    surface = Color(0xFFFFFFFF), surfaceGlass = Color(0xCCFFFFFF), surfaceBorder = Color(0x26150E3A),
    surfaceRaised = Color(0xFFFFFFFF), accent = Color(0xFFB86A00), accentSecondary = Color(0xFFC2500E),
    accentGlow = Color(0x40B86A00), onAccent = Color(0xFFFFFFFF), textPrimary = Color(0xFF15112E),
    textSecondary = Color(0xFF4F4878), textTertiary = Color(0xFF6E6896), success = Color(0xFF0E8F83),
    warning = Color(0xFF9A5B00), error = Color(0xFFC62840), userBubble = Color(0x33B86A00),
    assistantBubble = Color(0xCCFFFFFF), depthShadow = Color(0xFF6C4DFF), rimLight = Color(0xCCFFFFFF),
    lagoon = Color(0xFF3FE0D0), lotus = Color(0xFFFF6FAE),
)

val LocalNovaColors = compositionLocalOf { NovaDark }
val LocalNovaReducedMotion = compositionLocalOf { false }

val novaColors: NovaColorScheme
    @Composable get() = LocalNovaColors.current

object NovaSpacing { val xs = 4.dp; val sm = 8.dp; val md = 12.dp; val lg = 16.dp; val xl = 24.dp; val xxl = 32.dp; val xxxl = 48.dp }
object NovaShapes {
    val small = RoundedCornerShape(12.dp); val medium = RoundedCornerShape(20.dp)
    val large = RoundedCornerShape(28.dp); val pill = RoundedCornerShape(percent = 50)
}
object NovaMotion { const val FAST = 150; const val MEDIUM = 300; const val SLOW = 500 }

private fun Context.findActivity(): Activity? {
    var c = this
    while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
    return null
}

@Composable
fun NovaTheme(mode: ThemeMode, reducedMotion: Boolean, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val c = if (dark) NovaDark else NovaLight
    val scheme = if (dark) darkColorScheme(
        primary = c.accent, onPrimary = c.onAccent, secondary = c.accentSecondary, background = c.background,
        onBackground = c.textPrimary, surface = c.surface, onSurface = c.textPrimary, error = c.error,
        surfaceVariant = c.surfaceRaised, onSurfaceVariant = c.textSecondary, outline = c.surfaceBorder,
    ) else lightColorScheme(
        primary = c.accent, onPrimary = c.onAccent, secondary = c.accentSecondary, background = c.background,
        onBackground = c.textPrimary, surface = c.surface, onSurface = c.textPrimary, error = c.error,
        surfaceVariant = c.surfaceRaised, onSurfaceVariant = c.textSecondary, outline = c.surfaceBorder,
    )
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            view.context.findActivity()?.window?.let { w ->
                val ctl = WindowCompat.getInsetsController(w, view)
                ctl.isAppearanceLightStatusBars = !dark
                ctl.isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalNovaColors provides c, LocalNovaReducedMotion provides reducedMotion) {
        MaterialTheme(
            colorScheme = scheme,
            typography = NovaTypography,
            shapes = Shapes(small = NovaShapes.small, medium = NovaShapes.medium, large = NovaShapes.large),
            content = content,
        )
    }
}
