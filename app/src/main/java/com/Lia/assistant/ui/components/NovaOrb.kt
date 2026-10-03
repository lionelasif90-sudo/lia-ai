package com.Lia.assistant.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.Lia.assistant.ui.theme.LocalNovaReducedMotion
import com.Lia.assistant.ui.theme.NovaColorScheme
import com.Lia.assistant.ui.theme.novaColors
import com.Lia.assistant.voice.NovaOrbState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class NovaOrbStyle(val displayName: String) {
    NOVA("Nova"), AURORA("Aurora"), PLASMA("Plasma"), GLASS("Glass"),
    ENERGY("Energy"), MINIMAL("Minimal"), ARCHER("Archer");

    companion object { fun fromName(n: String?) = entries.firstOrNull { it.name == n } ?: NOVA }
}

data class OrbPalette(val core: Color, val rim: Color, val halo: Color)

fun orbPalette(state: NovaOrbState, c: NovaColorScheme): OrbPalette = when (state) {
    NovaOrbState.IDLE -> OrbPalette(c.lotus, c.textPrimary, c.lotus)
    NovaOrbState.LISTENING -> OrbPalette(c.lagoon, c.textPrimary, c.lagoon)
    NovaOrbState.THINKING -> OrbPalette(c.lotus, c.lagoon, c.lagoon)
    NovaOrbState.SPEAKING -> OrbPalette(c.accent, c.lotus, c.accent)
    NovaOrbState.CONNECTING -> OrbPalette(c.accentSecondary, c.accent, c.accentSecondary)
    NovaOrbState.ERROR -> OrbPalette(c.error.copy(alpha = 0.7f), c.error, c.error)
}

/** Canvas orb: fallback for API < 33 and the 7 selectable styles. */
@Composable
fun NovaOrb(
    state: NovaOrbState,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    style: NovaOrbStyle = NovaOrbStyle.NOVA,
    amplitude: Float = 0f,
    name: String = "Lia",
) {
    val c = novaColors
    val reduced = LocalNovaReducedMotion.current
    val pal = orbPalette(state, c)
    val t = rememberInfiniteTransition(label = "orb")
    val breathe by t.animateFloat(0f, (2 * PI).toFloat(), infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "breathe")
    val rotMs = when (state) { NovaOrbState.THINKING -> 2600; NovaOrbState.CONNECTING -> 1100; else -> 9000 }
    val rot by t.animateFloat(0f, 360f, infiniteRepeatable(tween(rotMs, easing = LinearEasing)), label = "rot")
    val ring by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "ring")

    val b = if (reduced) 0f else breathe
    val rotation = if (reduced) 0f else rot
    val ringP = if (reduced) 0.5f else ring

    Canvas(
        modifier.size(size).semantics { contentDescription = "$name status: ${state.name.lowercase()}" }
    ) {
        val cx = this.size.width / 2f
        val cy = this.size.height / 2f
        val center = Offset(cx, cy)
        val base = this.size.minDimension / 2f * 0.55f
        val r = base * (1f + 0.035f * sin(b) + amplitude.coerceIn(0f, 1f) * 0.15f)

        // outer glow
        drawCircle(
            Brush.radialGradient(listOf(pal.halo.copy(alpha = 0.45f), Color.Transparent), center, r * 1.8f),
            r * 1.8f, center,
        )
        // listening rings
        if (state == NovaOrbState.LISTENING) {
            for (k in 0..1) {
                val p = (ringP + k * 0.5f) % 1f
                drawCircle(pal.core.copy(alpha = (1f - p) * 0.5f), r * (1f + p * 0.6f), center, style = Stroke(2.dp.toPx()))
            }
        }
        // error pulse
        if (state == NovaOrbState.ERROR) {
            drawCircle(pal.rim.copy(alpha = 0.35f * (1f - ringP)), r * (1f + ringP * 0.5f), center, style = Stroke(2.dp.toPx()))
        }

        if (style == NovaOrbStyle.ARCHER) {
            val n = 400
            val golden = 2.3999632f
            val rad = rotation * (PI.toFloat() / 180f)
            for (i in 0 until n) {
                val y = 1f - (i / (n - 1f)) * 2f
                val rr = sqrt((1f - y * y).coerceAtLeast(0f))
                val th = i * golden + rad
                val x = cos(th) * rr
                val z = sin(th) * rr
                val depth = (z + 1f) / 2f
                val jitter = amplitude * 3.dp.toPx() * sin(i + b * 8f)
                val s = (0.6f + 1.8f * depth) * density
                drawCircle(
                    pal.core.copy(alpha = 0.2f + 0.8f * depth), s,
                    Offset(cx + x * (r * 1.15f + jitter), cy + y * (r * 1.15f + jitter)),
                )
            }
            return@Canvas
        }

        // connecting arc
        if (state == NovaOrbState.CONNECTING) {
            drawArc(pal.rim, rotation, 100f, false, Offset(cx - r * 1.2f, cy - r * 1.2f), Size(r * 2.4f, r * 2.4f),
                style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }

        // orbiting particles
        val count = when {
            style == NovaOrbStyle.MINIMAL || state == NovaOrbState.ERROR -> 0
            style == NovaOrbStyle.ENERGY -> 8
            style == NovaOrbStyle.PLASMA -> 6
            style == NovaOrbStyle.GLASS -> 3
            else -> 5
        }
        for (i in 0 until count) {
            val a = (rotation + i * 360f / count) * (PI.toFloat() / 180f)
            val orbit = r * (1.25f + 0.08f * (i % 3))
            drawCircle(pal.rim.copy(alpha = 0.85f), 3.dp.toPx(), Offset(cx + cos(a) * orbit, cy + sin(a) * orbit))
        }

        // core
        val coreBrush = when (style) {
            NovaOrbStyle.AURORA -> Brush.sweepGradient(listOf(pal.core, pal.halo, c.lagoon, pal.core), center)
            NovaOrbStyle.PLASMA -> Brush.radialGradient(listOf(pal.rim, pal.core, pal.halo.copy(alpha = 0.6f)), center, r)
            NovaOrbStyle.GLASS -> Brush.radialGradient(listOf(pal.core.copy(alpha = 0.25f), pal.core.copy(alpha = 0.6f)), center, r)
            else -> Brush.radialGradient(listOf(pal.rim.copy(alpha = 0.9f), pal.core, pal.core.copy(alpha = 0.7f)), center, r)
        }
        drawCircle(coreBrush, r, center)
        if (style == NovaOrbStyle.GLASS) {
            drawArc(Color.White.copy(alpha = 0.7f), 200f, 70f, false, Offset(cx - r * 0.8f, cy - r * 0.8f),
                Size(r * 1.6f, r * 1.6f), style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}
